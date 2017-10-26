#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "InputMonitorBuffer.h"
#include <PortDefs.h>
#include "ToJava.h"


InputMonitorBuffer::InputMonitorBuffer(int i_bufferSizeFrames, int i_channels)
: m_bufferSizeFrames(i_bufferSizeFrames),
  m_channels(i_channels)
{
    //wxLogDebugMain("InputMonitorBuffer allocated of m_bufferSizeFrames = %d, i_channels = %d", m_bufferSizeFrames, i_channels);
    m_data = new float[i_bufferSizeFrames * i_channels + 8];
    char *memoryBarrier = (char *) (m_data + m_bufferSizeFrames * m_channels);
    strcpy(memoryBarrier, "eXtream");

    m_currentWritePositionInBuffer = m_data;
    m_currentWritePositionInFrames = 0;
    m_framesWritten = 0;
    m_currentReadPositionInBuffer = m_data;
    m_currentReadPositionInFrames = 0;
}


InputMonitorBuffer::~InputMonitorBuffer()
{
    // test if eXtream was hit!
    char *memoryBarrier = (char *) (m_data + m_bufferSizeFrames * m_channels);
    if (strcmp(memoryBarrier, "eXtream") != 0)
    {
        logIt("Memory after InputMonitorBuffer was hit!");
        logIt("memoryBarrier = %s", memoryBarrier);
    }

    delete[] m_data;
}


bool InputMonitorBuffer::checkPointerInside(char *i_pointer) const
{
    char *data = (char *) m_data;
    char *endData = (char *) (m_data + m_bufferSizeFrames * m_channels);

    if (i_pointer < data || i_pointer >= endData)
    {
        logIt("POINTER OUTSIDE InputMonitorBuffer!!! data = %x, endData = %x, i_pointer = %x", data, endData, i_pointer);
        return false;
    }

    return true;
}


void InputMonitorBuffer::checkMemoryBarrierHit(const std::string& i_extraMessage) const
{
    // test if eXtream was hit!
    char *memoryBarrier = (char *) (m_data + m_bufferSizeFrames * m_channels);
    if (strcmp(memoryBarrier, "eXtream") != 0)
    {
        logError("Memory after input monitor buffer was hit: %s", i_extraMessage.c_str());
        logError("memoryBarrier = %s", memoryBarrier);
    }
}



void InputMonitorBuffer::copyToInputMonitorBuffer(float *i_data, int i_bufferSizeFrames)
{
    if (m_currentWritePositionInBuffer == NULL)
    {
        wxLogDebugMain("m_currentWritePositionInBuffer == NULL!");
        return;
    }

    if (m_data)
    {
        checkMemoryBarrierHit("in copyToInputMonitorBuffer START");

        //wxLogDebugMain("WRITE to %d", m_currentWritePositionInFrames);
        if (m_currentWritePositionInFrames + i_bufferSizeFrames < m_bufferSizeFrames) // easy: just copy
        {
            memcpy(m_currentWritePositionInBuffer, i_data, i_bufferSizeFrames * sizeof(float) * m_channels);
            m_currentWritePositionInFrames += i_bufferSizeFrames;
            m_currentWritePositionInBuffer += i_bufferSizeFrames * m_channels;

            //wxLogDebugMain("IMB: normal copy");
        }
        else // split into two copies
        {
            // copy to end
            int remainder = m_bufferSizeFrames - m_currentWritePositionInFrames;
            memcpy(m_currentWritePositionInBuffer, i_data, remainder * sizeof(float) * m_channels);

            // copy to start
            float *source = i_data + remainder * m_channels;
            remainder = i_bufferSizeFrames - remainder;
            memcpy(m_data, source, remainder * sizeof(float) * m_channels);
            m_currentWritePositionInFrames = remainder;
            m_currentWritePositionInBuffer = m_data + remainder * m_channels;

            //wxLogDebugMain("IMB: split copy");
        }

        m_framesWritten += i_bufferSizeFrames;

        checkMemoryBarrierHit("in copyToInputMonitorBuffer END");
    }
}


int InputMonitorBuffer::getFramesWritten() const
{
    return m_framesWritten;
}


void InputMonitorBuffer::copyFromBuffer(float *o_destDataPtr, int i_bufferSizeFrames)
{
    if (m_currentReadPositionInBuffer == NULL)
    {
        wxLogDebugMain("m_currentReadPositionInBuffer == NULL!");
        return;
    }

    if (m_data)
    {
        checkMemoryBarrierHit("in copyInputMonitorBufferToFeeder START");

        int latency = (m_currentWritePositionInFrames + m_bufferSizeFrames - m_currentReadPositionInFrames) % m_bufferSizeFrames;

        if (i_bufferSizeFrames > latency)
        {
            //logError("MON UNDERFLOW: i_bufferSizeFrames = %d, latency = %d", i_bufferSizeFrames, latency);
            memset(o_destDataPtr, 0, i_bufferSizeFrames * sizeof(float) * m_channels);
            return;
        }

        //wxLogDebugMain("READ latency = %d", latency);
        if (m_currentReadPositionInFrames + i_bufferSizeFrames < m_bufferSizeFrames) // easy: just copy
        {
            memcpy(o_destDataPtr, m_currentReadPositionInBuffer, i_bufferSizeFrames * sizeof(float) * m_channels);
            m_currentReadPositionInFrames += i_bufferSizeFrames;
            m_currentReadPositionInBuffer += i_bufferSizeFrames * m_channels;

            //wxLogDebugMain("IMB: normal copy");
        }
        else // split into two copies
        {
            // copy to end
            int remainder = m_bufferSizeFrames - m_currentReadPositionInFrames;
            memcpy(o_destDataPtr, m_currentReadPositionInBuffer, remainder * sizeof(float) * m_channels);

            // copy to start
            float *dest = o_destDataPtr + remainder * m_channels;
            remainder = i_bufferSizeFrames - remainder;
            memcpy(dest, m_data, remainder * sizeof(float) * m_channels);
            m_currentReadPositionInFrames = remainder;
            m_currentReadPositionInBuffer = m_data + remainder * m_channels;

            //wxLogDebugMain("IMB: split copy");
        }

        checkMemoryBarrierHit("in copyInputMonitorBufferToFeeder END");
    }
}


int InputMonitorBuffer::getChannels() const
{
    return m_channels;
}
