#pragma once

#include <string>

class InputMonitorBuffer
{
public:
    InputMonitorBuffer(int i_bufferSizeFrames, int i_channels);
    ~InputMonitorBuffer();

    bool checkPointerInside(char *i_pointer) const;
    void checkMemoryBarrierHit(const std::string& i_extraMessage) const;

    void copyToInputMonitorBuffer(float *i_data, int i_bufferSizeFrames);
    int getFramesWritten() const;

    void copyFromBuffer(float *o_destDataPtr, int i_bufferSizeFrames);

    int getChannels() const;

private:
    float *m_data;
    int m_bufferSizeFrames;
    int m_channels;

    float *m_currentWritePositionInBuffer;
    int m_currentWritePositionInFrames;
    int m_framesWritten;

    float *m_currentReadPositionInBuffer;
    int m_currentReadPositionInFrames;
};
