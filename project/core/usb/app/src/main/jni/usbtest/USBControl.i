%include <std_string.i>

%nodefaultctor USBControl;
class USBControl
{
public:
    USBControl();
    virtual ~USBControl();

    bool initUSB(bool i_enumerateDevices);
    void cleanUp();

    //USBAudioManager *getUSBAudioManager() const;
    void restartUSBAudioManager(bool i_enumerateDevices);

    bool initUSBDevice(int i_deviceFd, int i_productId, int i_vendorId);
    bool initUSBDeviceByName(int i_deviceFd, std::string i_deviceName, int i_productId, int i_vendorId, jbyteArray i_rawDescriptors, int i_rawDescriptionsLength);
    std::string getOpenDeviceErrorMessage();

    bool isAudioDevicePresentForLinux();

    bool startAudioTransfers(bool i_playAudio,
                             bool i_recordAudio,
                             int i_sampleRate,
                             bool i_force1PPT,
                             int i_bufferSizeInFrames,
                             int i_openSLESBufferSizeInFrames);
    void stopAudioTransfers();
    bool startMIDIInputTransfers();
    void stopMIDIInputTransfers();

    void setUSBSampleRate(int i_sampleRate);

    bool startOpenSLES(bool i_play,
                       bool i_record,
                       int i_actualChannelsRecording,
                       int i_sampleRate,
                       int i_bufferSizeInFrames);

    void stopOpenSLES();

    void setObjectToPassArrayTo(jobject i_mainActivity);

    void sendMidiMessage(int i_status, int i_byte1, int i_byte2);
};
