#include "USBControl.h"
#include <IVolumeController.h>
#include <USBAudioManager.h>
#include <USBAudioDevice.h>
#include <USBAudioStreamConfig.h>
#ifdef MIDI_SUPPORT
#include <USBMidiStreamConfig.h>
#endif
#include <USBInterruptConfig.h>
#include <string.h>
#include "InputMonitorBuffer.h"
#include "PortDefs.h"
#include "ToJava.h"

const bool s_useMidiIn = true;
const bool s_useMidiOut = true;


USBControl::USBControl()
{
    wxLogDebugMain("New USBControl!");
    m_USBAudioManager = NULL;
    m_playing = false;
    m_recording = false;
    m_openSlStream = NULL;

    m_inputMonitoringBuffer = NULL;
    m_tempOutputBuffer = NULL;
    m_channelConversionBuffer = NULL;
    m_activityObject = NULL;
    m_convertBuffer = NULL;
    
    setLogMethod(2);
}


USBControl::~USBControl()
{
    cleanUp();

    if (m_activityObject)
    {
        GetEnv()->DeleteLocalRef(m_activityObject);
        m_activityObject = NULL;
    }
}


USBAudioManager *USBControl::getUSBAudioManager() const
{
	return m_USBAudioManager;
}


bool USBControl::initUSB(bool i_enumerateDevices)
{
    // test for passing float to Java:
    /*
    float* floats = new float[10];
    for (int i = 0; i < 10; i++)
    {
        floats[i] = 1.0f + float(i) / 10.0f;
    }
    passFloatsToJava(floats, 10);
    delete[] floats;
    */
    logIt( "initUSB" );

	if (m_USBAudioManager == NULL)
	{
		m_USBAudioManager = new USBAudioManager("", -1, -1, false);
        return m_USBAudioManager->init(i_enumerateDevices);
    }

    return true;
}


void USBControl::cleanUp()
{
    if (m_USBAudioManager)
	{
		delete m_USBAudioManager;
		m_USBAudioManager = NULL;
	}

    if (m_inputMonitoringBuffer)
    {
        delete m_inputMonitoringBuffer;
        m_inputMonitoringBuffer = NULL;
    }
    if (m_tempOutputBuffer != NULL)
    {
        delete[] m_tempOutputBuffer;
        m_tempOutputBuffer = NULL;
    }

    if (m_convertBuffer != NULL)
    {
        delete[] m_convertBuffer;
        m_convertBuffer = NULL;
    }

    if (m_channelConversionBuffer != NULL)
    {
        delete[] m_channelConversionBuffer;
        m_channelConversionBuffer = NULL;
    }
}


void USBControl::restartUSBAudioManager(bool i_enumerateDevices)
{
    if (m_USBAudioManager != NULL)
    {
        delete m_USBAudioManager;
        m_USBAudioManager = NULL;
    }

    initUSB(i_enumerateDevices);
}


void USBControl::interruptCallbackFunction(int i_ID, void *i_userData)
{
    wxLogDebugMain("interruptCallbackFunction! ID = %d, i_userData = %p", i_ID, i_userData);

    USBControl* usbControl = (USBControl *)i_userData;

    if (usbControl->m_USBAudioManager)
    {
        USBAudioDevice *device = usbControl->m_USBAudioManager->getCurrentAudioDevice();

        if (device)
        {
            std::vector < IVolumeController * > volumeControllers = device->getAllVolumeControllers();

            for (size_t i = 0; i < volumeControllers.size(); i++)
            {
                if (volumeControllers[i]->getID() == i_ID)
                {
                    // do something
                    break;
                }
            }
        }
    }
}


void USBControl::prepareMidiDevices(USBAudioDevice* i_device)
{
#ifdef MIDI_SUPPORT
    std::vector< USBMidiStreamConfig* >& midiOutStreamConfigs = i_device->getMidiOutputStreamConfigs();
    for (size_t i = 0; i < midiOutStreamConfigs.size(); i++)
    {
        wxLogDebugMain("Prepare out");
        midiOutStreamConfigs[i]->prepareForOutput(256);
    }

    std::vector< USBMidiStreamConfig* >& midiInStreamConfigs = i_device->getMidiInputStreamConfigs();
    for (size_t i = 0; i < midiInStreamConfigs.size(); i++)
    {
        wxLogDebugMain("Prepare in");
        midiInStreamConfigs[i]->prepareForInput(1);
    }
#endif
}


bool USBControl::initUSBDevice(int i_deviceFd, int i_productId, int i_vendorId)
{
    if (m_USBAudioManager != NULL)
	{
        bool askResetDevice = false;
        int dummyStackTrace = 0;

        wxLogDebugMain("initUSBDevice");
        USBAudioDevice *device = m_USBAudioManager->OpenDeviceFD(i_deviceFd, i_productId, i_vendorId, &askResetDevice, false, dummyStackTrace, true, s_useMidiIn, s_useMidiOut, true);

        wxLogDebugMain("device = %p", device);
        if (device)
        {
            setAudioControlInterruptCallback(device);

            if (s_useMidiIn || s_useMidiOut)
            {
                prepareMidiDevices(device);
            }
        }

        return (device != NULL);
    }

    return false;
}


bool USBControl::initUSBDeviceByName(int i_deviceFd, std::string i_deviceName, int i_productId, int i_vendorId, jbyteArray i_rawDescriptors, int i_rawDescriptionsLength)
{
    if (m_USBAudioManager != NULL)
    {
        wxLogDebugMain("initUSBDevice2");
        bool askResetDevice = false;
        int dummyStackTrace = 0;

        jboolean isCopy;
        jbyte* rawBytes = GetEnv()->GetByteArrayElements(i_rawDescriptors, &isCopy);
        if (rawBytes == NULL)
        {
            return false;
        }

        USBAudioDevice *device = m_USBAudioManager->OpenDeviceFDAndName(i_deviceFd, i_deviceName, i_productId, i_vendorId, &askResetDevice, false, dummyStackTrace, true, s_useMidiIn, s_useMidiOut, true,
                                                                        (unsigned char *)rawBytes, i_rawDescriptionsLength, USBAudioDevice::BUS_SPEED_AUTOMATIC);

        GetEnv()->ReleaseByteArrayElements(i_rawDescriptors, rawBytes, 0);

        if (device)
        {
            setAudioControlInterruptCallback(device);
            if (s_useMidiIn || s_useMidiOut)
            {
                prepareMidiDevices(device);
            }
        }

        return (device != NULL);
    }

    return false;
}


void USBControl::setAudioControlInterruptCallback(USBAudioDevice *i_device)
{
    if (i_device)
    {
        std::vector< USBInterruptConfig* >& configs = i_device->getInterruptInputStreamConfigs();
        for (size_t i = 0; i < configs.size(); i++)
        {
            if (configs[i]->getUSBAltSetting()->getSubClass() == USBAltSetting::AUDIOINTERFACE_SUBCLASS_AUDIOCONTROL &&
                configs[i]->getUSBAltSetting()->getAlternateSettingNr() == 0 && configs[i]->getUSBAltSetting()->getInterfaceNumber() == 0)
            {
                wxLogDebugMain("Found correct interrupt config!");
                configs[i]->setInterruptCallback(interruptCallbackFunction, this);
                break;
            }
        }
    }
}


bool USBControl::isAudioDevicePresentForLinux()
{
    if (m_USBAudioManager != NULL)
	{
        return m_USBAudioManager->isAudioDevicePresentForLinux();
    }

    return false;
}


std::string USBControl::getOpenDeviceErrorMessage()
{
    if (m_USBAudioManager != NULL)
	{
        return m_USBAudioManager->getOpenDeviceErrorMessage();
    }
    else
    {
        return "";
    }
}

bool USBControl::playCallback(void *o_output,
                              unsigned int i_frameCount,
                              USBAudioStreamConfig *i_streamConfig,
                              void *i_userData,
                              bool i_preFill,
                              bool i_fillSilence,
                              unsigned int& o_frameCount)
{
    USBControl *usbControl = (USBControl *) i_userData;
    InputMonitorBuffer* inputMonitorBuffer = NULL;

    if (usbControl->m_playing && usbControl->m_recording) // route USB or OpenSLES input to output, use input monitoring buffer
    {
        inputMonitorBuffer = usbControl->m_inputMonitoringBuffer;
    }

    //wxLogDebugMain("USB playcb usbControl->m_playing = %d, usbControl->m_recording = %d, inputMonitorBuffer = %p", usbControl->m_playing, usbControl->m_recording, inputMonitorBuffer);

    o_frameCount = i_frameCount;

    /*if (inputMonitorBuffer)
    {
        wxLogDebugMain("USB playcb!, framecount = %u, frames written = %d", i_frameCount, inputMonitorBuffer->getFramesWritten());
    }*/

    // check if we have enough audio data in our input monitoring buffer
    if ((usbControl->m_playing || i_preFill) && o_output && inputMonitorBuffer && inputMonitorBuffer->getFramesWritten() >= i_frameCount)
    {
        const int items = i_frameCount * i_streamConfig->getUSBAltSetting()->getNumberOfChannels();

        // first make sure we have an output buffer containing all floating point data that is equal in number of channels as the output altsetting selected
        usbControl->mapInputChannelsToOutputChannels(i_frameCount, i_streamConfig->getUSBAltSetting()->getNumberOfChannels());

        // after mapInputChannelsToOutputChannels(), m_tempOutputBuffer contains the floating point data for output, now scale it to the output
        // resolution
        float *outputBuffer = usbControl->m_tempOutputBuffer;

        /* Fill the output buffer o_output here with data of all output channels*/
        if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 2) // 16-bit
        {
            short *outputData = (short *) o_output;

            for (int i = 0; i < items; i++)
            {
                outputData[i] = (short) (outputBuffer[i] * 32767.0);
            }
        }
        else if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 3)
        {
            // convert from float to 24-bit packed buffer
            int value;
            unsigned char *DestPtr = (unsigned char *) o_output;

            for (int i = 0; i < items; i++)
            {
                value = (int) (outputBuffer[i] * 8388607.0);
                memcpy(DestPtr, &value, 3);
                DestPtr += 3;
            }
        }
        else if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 4)
        {
            int *outputData = (int *) o_output;
            const float scale = (float) INT_MAX;

            for (int i = 0; i < items; i++)
            {
                outputData[i] = (int) (outputBuffer[i] * scale);
            }
        }

        return true;
    }
    else if (o_output)
    {
        memset(o_output, 0, i_frameCount * i_streamConfig->getUSBAltSetting()->getTotalFrameSize());
    }

    return true;
}


bool USBControl::recordCallback(const void *i_input,
                                unsigned int i_frameCount,
                                USBAudioStreamConfig * i_streamConfig,
                                void *i_userData)
{
    USBControl *usbControl = (USBControl *) i_userData;

    // convert everything to floating point to handle all source data types conveniently (1, 2, 3 and 4-byte data)

    //wxLogDebugMain("recCB!");
    if( usbControl->m_recording && i_input && usbControl->m_inputMonitoringBuffer )
    {
        const int items = i_frameCount * i_streamConfig->getUSBAltSetting()->getNumberOfChannels();
        //wxLogDebugMain("items = %d", items);
        float *tmpBuffer = usbControl->m_convertBuffer;

        bool toPass = false;

        //wxLogDebugMain("USB rec cb! ch = %d", i_streamConfig->getUSBAltSetting()->getNumberOfChannels());
        if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 2)
        {
            const float factor = 1.0f / 32768.0f;
            short *sourcePtr = (short *) i_input;

            // convert short to float
            for (int i = 0; i < items; i++)
            {
                *tmpBuffer++ = *sourcePtr++ * factor;
            }

            toPass = true;
        }
        else if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 3)
        {
            usbControl->convert24to32Bit(i_input, i_frameCount * i_streamConfig->getUSBAltSetting()->getNumberOfChannels());
        }
        else if (i_streamConfig->getUSBAltSetting()->getSubFrameSize() == 4)
        {
            const float factor = 1.0f / INT_MAX;
            int *sourcePtr = (int *) i_input;

            // convert int to float
            for (int i = 0; i < items; i++)
            {
                *tmpBuffer++ = *sourcePtr++ * factor;
            }
        }

        usbControl->m_inputMonitoringBuffer->copyToInputMonitorBuffer(usbControl->m_convertBuffer, i_frameCount);
        //if( toPass == true ) usbControl->passFloatsToJava( usbControl->m_convertBuffer, i_frameCount );
        if( toPass == true ) usbControl->passShortsToJava( (short *) i_input, i_frameCount );

        return true;
    }

    return true;
}


void USBControl::mapInputChannelsToOutputChannels(const int i_frameCount, const int i_outputChannels)
{
    //wxLogDebugMain("in map: m_inputMonitoringBuffer = %p, i_frameCount = %d", m_inputMonitoringBuffer, i_frameCount);
    if (m_inputMonitoringBuffer->getChannels() == i_outputChannels)
    {
        // easy: same number of channels for input and output: just copy the data from the input monitoring data directory to the temporary floating point output buffer
        m_inputMonitoringBuffer->copyFromBuffer(m_tempOutputBuffer, i_frameCount);
    }
    else // we have less or more input channels than output channels
    {
        //wxLogDebugMain("in = %d, out = %d", m_inputMonitoringBuffer->getChannels(), i_outputChannels);
        // first copy the input monitoring buffer to the m_channelConversionBuffer
        m_inputMonitoringBuffer->copyFromBuffer(m_channelConversionBuffer, i_frameCount);

        // Then copy the input to all outputs. If there are more outputs then inputs, the other outputs will get a copy of the inputs. For example,
        // with 2 inputs and 4 outputs, output 1 will be input 1, out 2 = in 2, out 3 = in 1, out 4 = in 2

        // You could do other processing on m_channelConversionBuffer here to produce audio for all output channels
        //wxLogDebugMain("m_tempOutputBuffer = %p, m_inputMonitoringBuffer = %p", m_tempOutputBuffer, m_inputMonitoringBuffer);

        const int inputs = m_inputMonitoringBuffer->getChannels();
        const int outputs = i_outputChannels;
        float *inputPtr = m_channelConversionBuffer;
        float *outputPtr = m_tempOutputBuffer;

        for (int i = 0; i < i_frameCount; i++)
        {
            for (int p = 0; p < outputs; p++)
            {
                *outputPtr++ = *(inputPtr + (p % inputs));
            }

            inputPtr += inputs;
        }
    }
}


float *USBControl::convert24to32Bit(const void *i_input,
                                    unsigned int i_items)
{
    unsigned char *sourcePtr = (unsigned char *) i_input;
    float *destPtr = m_convertBuffer;
    int value;
    const float factor = 1.0f / INT_MAX;

    for (unsigned int i = 0; i < i_items; i++)
    {
        value = (sourcePtr[2] << 24) | (sourcePtr[1] << 16) | (sourcePtr[0] << 8);

        destPtr[i] = value * factor;
        sourcePtr += 3;
    }

    return m_convertBuffer;
}

#ifdef MIDI_SUPPORT
void MidiRecordCallback(PmEvent i_event,
                        USBMidiStreamConfig *i_streamConfig,
                        void *i_userData)
{
    wxLogDebugMain("MIDI received: %x %x %x", Pm_MessageStatus(i_event.message), Pm_MessageData1(i_event.message), Pm_MessageData2(i_event.message));
}
#endif

void USBControl::sendMidiMessage(int i_status, int i_byte1, int i_byte2)
{
#ifdef MIDI_SUPPORT
    if (m_USBAudioManager)
    {
        std::vector<USBAudioDevice *> midiDevices = m_USBAudioManager->getAllMIDICapableDevices();
        if (midiDevices.size() > 0)
        {
            USBAudioDevice *audioDevice = midiDevices[0];

            if (audioDevice)
            {
                std::vector< USBMidiStreamConfig* >& midiStreamConfigs = audioDevice->getMidiOutputStreamConfigs();

                if (midiStreamConfigs.size() > 0)
                {
                    // assume we want to send on the first and only MIDI device
                    midiStreamConfigs[0]->submitNextOutputTransfer(i_status, i_byte1, i_byte2);
                }
            }
            else
            {
                wxLogDebugMain("No MIDI device");
            }
        }
    }
#endif
}


void USBControl::sendMidiMessage(const std::vector<unsigned char>& i_data)
{
#ifdef MIDI_SUPPORT
    std::vector<USBAudioDevice *> midiDevices = m_USBAudioManager->getAllMIDICapableDevices();
    if (midiDevices.size() > 0)
    {
        USBAudioDevice *audioDevice = midiDevices[0];

        if (audioDevice)
        {
            std::vector< USBMidiStreamConfig* >& midiStreamConfigs = audioDevice->getMidiOutputStreamConfigs();

            if (midiStreamConfigs.size() > 0)
            {
                // assume we want to send on the first and only MIDI device
                midiStreamConfigs[0]->submitNextOutputTransfer(i_data);
            }
        }
        else
        {
            wxLogDebugMain("No MIDI device");
        }
    }
#endif
}


bool USBControl::startAudioTransfers(bool i_playUSBAudio,
                                     bool i_recordUSBAudio,
                                     int i_sampleRate,
                                     bool i_force1PPT,
                                     int i_bufferSizeInFrames,
                                     int i_openSLESBufferSizeInFrames)
    {
    bool routeUSBinToUSBout = i_playUSBAudio && i_recordUSBAudio;
    /*if (m_playing)
    {
        wxLogDebugMain("Already playing!");
        return false;
    }*/

    wxLogDebugMain("StartUSBTransfers");

    if (m_playing)
    {
        wxLogErrorMain("startAudioTransfers called when already playing!");

        return false;
    }

    if (m_USBAudioManager == NULL)
    {
        wxLogErrorMain("m_USBAudioManager was NULL!");
        return false;
    }

    USBAudioDevice *audioDevice = m_USBAudioManager->getCurrentAudioDevice();

    if (m_USBAudioManager && audioDevice)
    {
        audioDevice->setPlayCallback(playCallback, this);
        audioDevice->setRecordCallback(recordCallback, this);

        USBAudioStreamConfig *playStreamConfig = audioDevice->getCurrentOutputStreamConfig();
        USBAudioStreamConfig *recordStreamConfig = audioDevice->getCurrentInputStreamConfig();

        m_playing = false;

        if (m_tempOutputBuffer)
        {
            delete[] m_tempOutputBuffer;
        }

        if (i_playUSBAudio)
        {
            int highestResolution = audioDevice->getHighestResolution(audioDevice->getAvailableOutputResolutions());
            wxLogDebugMain("Highest playback resolution = %d", highestResolution);
            audioDevice->setOutputResolution(highestResolution);
            playStreamConfig = audioDevice->getCurrentOutputStreamConfig();

            if (audioDevice->selectOutput(playStreamConfig) == false)
            {
                wxLogDebugMain("Error selecting output!");
            }

            m_tempOutputBuffer = new float[i_bufferSizeInFrames * playStreamConfig->getUSBAltSetting()->getNumberOfChannels()];

            //wxLogDebugMain("prepareForPlayBack, buffer size = %d", GetSampleBufferSize());
			int playBytesSize = audioDevice->prepareForPlayBack(i_sampleRate, i_bufferSizeInFrames, i_force1PPT, false, -1, false, false);
            if (playBytesSize == 0)
            {
                wxLogDebugMain("prepareForPlayBack failed!");
                return false;
            }
        }
        else
        {
            m_tempOutputBuffer = new float[i_openSLESBufferSizeInFrames * 2];
        }

        if (m_inputMonitoringBuffer)
        {
            delete m_inputMonitoringBuffer;
        }

        if (m_channelConversionBuffer)
        {
            delete[] m_channelConversionBuffer;
        }

        if (m_convertBuffer)
        {
            delete[] m_convertBuffer;
        }

        if (i_recordUSBAudio)
        {
            wxLogDebugMain("Prepare USB audio record");

            int highestResolution = audioDevice->getHighestResolution(audioDevice->getAvailableInputResolutions());
            wxLogDebugMain("Highest record resolution = %d", highestResolution);
            audioDevice->setInputResolution(highestResolution);
            recordStreamConfig = audioDevice->getCurrentInputStreamConfig();

            if (recordStreamConfig == NULL)
            {
                return false;
            }

            if (recordStreamConfig)
            {
                int nrOfChannels = recordStreamConfig->getUSBAltSetting()->getNumberOfChannels();

                m_inputMonitoringBuffer = new InputMonitorBuffer(32768, nrOfChannels);
                m_channelConversionBuffer = new float[nrOfChannels * 32768];
                int maxRealBufferSize;

                int recBytesSize = audioDevice->prepareForRecording(i_sampleRate, i_bufferSizeInFrames, i_force1PPT, maxRealBufferSize, false);
                if (recBytesSize > 0)
                {
                    int recordBufferSizeFrames = recBytesSize / recordStreamConfig->getUSBAltSetting()->getTotalFrameSize();
                    m_convertBuffer = new float[nrOfChannels * recordBufferSizeFrames];

                    //wxLogDebugMain("s_RecordBufferSizeFrames = %d, recBytesSize = %d", recordBufferSizeFrames, recBytesSize);
                }
                else
                {
                    wxLogDebugMain("prepareForRecording failed!");
                    return false;
                }
            }
            else
            {
                wxLogDebugMain("No input stream config selected!");
                return false;
            }
        }
        else // record from OpenSLES (mic)
        {
            wxLogDebugMain("Record from OpenSLES mic");

            m_convertBuffer = new float[1 * 32768];
            m_inputMonitoringBuffer = new InputMonitorBuffer(32768, 1);
            m_channelConversionBuffer = new float[1 * 32768];
        }

        if (routeUSBinToUSBout == false)
        {
            wxLogDebugMain("Starting OpenSLES");
            if (startOpenSLES(!i_playUSBAudio, !i_recordUSBAudio, 1, i_sampleRate, i_openSLESBufferSizeInFrames))
            {
                m_playing = true;
                m_recording = true;
                
                bool started = audioDevice->startAudioTransfers(i_playUSBAudio, i_recordUSBAudio);

                if (started == false)
                {
                    wxLogErrorMain("Failed to start transfers!");
                    m_playing = false;
                    m_recording = false;
                }

                return started;
            }
            else
            {
                logError("Failed to start OpenSLES!");
            }
        }
        else // do not use OpenSLES
        {
            m_playing = true;
            m_recording = true;

            wxLogDebugMain("Route USB input to output: startAllTransfers");
            bool started = audioDevice->startAudioTransfers(i_playUSBAudio, i_recordUSBAudio);

            if (started == false)
            {
                logError("Failed to start transfers!");
                m_playing = false;
                m_recording = false;
            }
            else
            {
                logError("Started OK!");
            }

            return started;
        }
    }
    else
    {
        if (m_USBAudioManager)
        {
            wxLogDebugMain("Strange: m_USBAudioManager->getCurrentAudioDevice() = %p", m_USBAudioManager->getCurrentAudioDevice());
        }
    }

    return false;
}


bool USBControl::startMIDIInputTransfers()
{
    if (m_USBAudioManager == NULL)
    {
        wxLogErrorMain("m_USBAudioManager was NULL!");
        return false;
    }

#ifdef MIDI_SUPPORT
    USBAudioDevice *audioDevice = m_USBAudioManager->getCurrentAudioDevice();

    if (m_USBAudioManager && audioDevice)
    {
        // set callback for incoming MIDI events
        std::vector<USBAudioDevice *> midiDevices = m_USBAudioManager->getAllMIDICapableDevices();
        for (size_t deviceNr = 0; deviceNr < midiDevices.size(); deviceNr++)
        {
            wxLogDebugMain("set MIDI record callback!");
            midiDevices[deviceNr]->setMidiRecordCallback(MidiRecordCallback, this);
        }

        wxLogDebugMain("Starting midi input transfers");
        bool started = audioDevice->startMIDIInputTransfers();

        wxLogDebugMain("started = %d", started);

        if (started == false)
        {
            wxLogDebugMain("Failed to start transfers!");
        }
        else
        {
            wxLogDebugMain("Started OK!");
        }

        return started;
    }
    else
    {
        if (m_USBAudioManager)
        {
            wxLogDebugMain("Strange: m_USBAudioManager->getCurrentAudioDevice() = %p", m_USBAudioManager->getCurrentAudioDevice());
        }
    }
#endif
    return false;
}


void USBControl::stopAudioTransfers()
{
    wxLogDebugMain("stopUSBTransfers");

    if (m_USBAudioManager)
    {
        USBAudioDevice *audioDevice = m_USBAudioManager->getCurrentAudioDevice();

        if (audioDevice)
        {
            audioDevice->stopAudioTransfers();
        }
    }

    stopOpenSLES();

    if (m_tempOutputBuffer != NULL)
    {
        delete[] m_tempOutputBuffer;
        m_tempOutputBuffer = NULL;
    }

    m_playing = false;
}


void USBControl::stopMIDIInputTransfers()
{
#ifdef MIDI_SUPPORT
    if (m_USBAudioManager)
    {
        USBAudioDevice *audioDevice = m_USBAudioManager->getCurrentAudioDevice();

        if (audioDevice)
        {
            audioDevice->stopMIDIInputTransfers();
        }
    }
#endif
}


void USBControl::setUSBSampleRate(int i_sampleRate)
{
    if (m_USBAudioManager)
    {
	    USBAudioDevice *device = m_USBAudioManager->getCurrentAudioDevice();

        if (device)
        {
            device->setSampleRate(i_sampleRate);
        }
    }
}


void USBControl::openSLCallback(void *context, int sample_rate, int buffer_frames,
                                int input_channels, const short *input_buffer,
                                int output_channels, short *output_buffer)
{
    //logIt("OpenSLCallback %d frames", buffer_frames);
    USBControl *usbControl = (USBControl *) context;
    
    // First rec part
    if (input_channels > 0 && input_buffer)
    {
        float *tmpBuffer = usbControl->m_convertBuffer;
        const int items = buffer_frames * input_channels;
        const float factor = 1.0f / 32768.0;
        short *sourcePtr = (short *) input_buffer;

        //wxLogDebugMain("Rec SLES");

        // convert short to float
        for (int i = 0; i < items; i++)
        {
            *tmpBuffer++ = *sourcePtr++ * factor;
        }

        usbControl->m_inputMonitoringBuffer->copyToInputMonitorBuffer(usbControl->m_convertBuffer, buffer_frames);
    }

    // then play part
    if (output_channels > 0 && output_buffer && usbControl->m_inputMonitoringBuffer)
    {
        if (usbControl->m_inputMonitoringBuffer->getFramesWritten() >= buffer_frames)
        {
            usbControl->mapInputChannelsToOutputChannels(buffer_frames, 2);
            
            float *outputBuffer = usbControl->m_tempOutputBuffer;

            const int items = buffer_frames * 2;

            for (int i = 0; i < items; i++)
            {
                output_buffer[i] = (short) (outputBuffer[i] * 32767.0);
            }
        }
    }
}


bool USBControl::startOpenSLES(bool i_play,
                               bool i_record,
                               int i_actualChannelsRecording,
                               int i_sampleRate,
                               int i_bufferSizeInFrames)
{
    m_openSlStream = opensl_open(i_sampleRate, i_record ? i_actualChannelsRecording : 0, i_play ? 2 : 0, i_bufferSizeInFrames, openSLCallback, this);

    if (m_openSlStream == NULL)
    {
        logError("Failed to open OpenSL stream!");
        return false;
    }

    if (i_record)
    {
        logIt("Prepare record OpenSLES");
    }

    bool started = (opensl_start(m_openSlStream) == 0);

    if (started == false)
    {
        logError("Failed to start transfers!");
    }
    else
    {
        logIt("Started OK!");
    }

    return started;
}


void USBControl::stopOpenSLES()
{
    if (m_openSlStream)
    {
        opensl_close(m_openSlStream);
        m_openSlStream = NULL;
    }
}


// called from the usbaudiostatic part, you can ignore this
void SetMainSampleRate(int Rate)
{
}



void USBControl::setObjectToPassArrayTo(jobject i_mainActivity)
{
    m_activityObject = GetEnv()->NewGlobalRef(i_mainActivity);

    jclass activityClass = GetEnv()->GetObjectClass(m_activityObject);

    if (GetEnv()->ExceptionOccurred())
    {
        logError("activityClass not found!");
        GetEnv()->ExceptionClear();
        return;
    }

    if (activityClass)
    {
        m_putAudioDataMethodID = GetEnv()->GetMethodID(activityClass, "putAudioData", "([S)V");
        // m_putAudioDataMethodID2 = GetEnv()->GetMethodID(activityClass, "putAudioData2", "([F)V");

        if (GetEnv()->ExceptionOccurred())
        {
            logError("putAudioData not found!");
            GetEnv()->ExceptionClear();
        }

        GetEnv()->DeleteLocalRef(activityClass);
    }
}


void USBControl::passShortsToJava(short * i_data, int i_numberOfShorts)
{
    // Allocate a jobjectArray of i_numberOfFloats java.lang.Float's
    jshortArray javaArray = (jshortArray) GetEnv()->NewShortArray(i_numberOfShorts);

    jboolean isCopy;
    jshort * buffer = GetEnv()->GetShortArrayElements(javaArray, &isCopy);
    if (buffer == NULL)
    {
        logError("buffer pointer was NULL!");
        GetEnv()->DeleteLocalRef(javaArray);
        return;
    }

    memcpy(buffer, i_data, i_numberOfShorts * sizeof(short));
    GetEnv()->ReleaseShortArrayElements(javaArray, buffer, 0);

    GetEnv()->CallVoidMethod(m_activityObject, m_putAudioDataMethodID, javaArray);

    if (GetEnv()->ExceptionOccurred())
    {
        logError("Exception in calling putAudioData!");
        GetEnv()->ExceptionClear();
    }

    GetEnv()->DeleteLocalRef(javaArray);     // release
}
/*
void USBControl::passFloatsToJava(float* i_data, int i_numberOfFloats)
{
    // Allocate a jobjectArray of i_numberOfFloats java.lang.Float's
    jfloatArray javaArray = (jfloatArray) GetEnv()->NewFloatArray(i_numberOfFloats);

    jboolean isCopy;
    jfloat* buffer = GetEnv()->GetFloatArrayElements(javaArray, &isCopy);
    if (buffer == NULL)
    {
        logError("buffer pointer was NULL!");
        GetEnv()->DeleteLocalRef(javaArray);
        return;
    }

    memcpy(buffer, i_data, i_numberOfFloats * sizeof(float));
    GetEnv()->ReleaseFloatArrayElements(javaArray, buffer, 0);

    GetEnv()->CallVoidMethod(m_activityObject, m_putAudioDataMethodID2, javaArray);

    if (GetEnv()->ExceptionOccurred())
    {
        logError("Exception in calling putAudioData!");
        GetEnv()->ExceptionClear();
    }

    GetEnv()->DeleteLocalRef(javaArray);     // release 
}*/
