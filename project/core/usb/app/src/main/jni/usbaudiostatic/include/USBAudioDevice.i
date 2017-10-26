%include "std_vector.i"
%include "std_string.i"
%include "IVolumeController.i"
%include "USBSelectorUnit.i"
%include "AudioDevice.i"
%include "AudioMode.i"
%include "USBAudioManager.i"

namespace std {
   %template(IVolumeControllerVector) vector< IVolumeController* >;
   %template(USBSelectorUnitVector) vector< USBSelectorUnit* >;
};

class USBAudioDevice
  : public AudioDevice
{
public:
    bool hasAudioInput() const;
    bool hasAudioOutput() const;
    bool hasMIDIInput() const;
    bool hasMIDIOutput() const;
    bool isMIDIOnly() const;

    bool startAllTransfers(bool i_playAudio, bool i_recordAudio, bool i_recordMidi);

    /// Stops all transfers and waits for them to finish. Returns measured audio latency in case of audio recording.
    int stopAllTransfers();

    std::vector< IVolumeController * > getAllVolumeControllers() const;
    std::vector< USBSelectorUnit * > getAllSelectorUnits() const;

    unsigned short getVendorID();
	unsigned short getProductID();

	std::string getManufacturerString();
	std::string getProductString();
	std::string getSerialNumberString();
};

