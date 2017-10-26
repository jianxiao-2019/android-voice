%include "std_string.i"
%include "AudioDevice.i"

%nodefaultctor USBAudioManager;
class USBAudioManager
{
public:
    std::string getOpenDeviceErrorMessage() const;

    bool isAudioDevicePresentForLinux() const;
    bool isMidiOutputDevicePresent() const;
#ifdef SUPPORTS_TRACK
    AEMAudioDevice *getCurrentAudioDeviceAsAudioDevice() const;
#else
    AudioDevice *getCurrentAudioDeviceAsAudioDevice() const;
#endif

    bool isAudioDevicePresent() const;
    bool isMidiDevicePresent() const;

    void setFirmwareDirectory(std::string i_firmwareDirectory);
    void doThreadMagic(int s);
    bool isUltraLowLatency() const;
};