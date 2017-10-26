%include "std_string.i"
%include "std_vector.i"
%include <AudioMode.i>

namespace std {
   %template(IntVector) vector< int >;
};

%nodefaultctor AudioDevice;
class AudioDevice
{
public:
    AudioMode* getCurrentOutputMode() const;
    void setCurrentOutputMode(AudioMode* i_mode);

    int getNumberOfInputModes() const;
    int getNumberOfOutputModes() const;

    void setLastUsedInputMode(AudioMode* i_mode);

    AudioMode* getInputMode(int i_index) const;
    AudioMode* getOutputMode(int i_index) const;

    std::string getName() const;

    int audioInputModeToIndex(AudioMode* i_mode) const;
    int audioOutputModeToIndex(AudioMode* i_mode) const;

    virtual bool setSampleRate(int i_sampleRate);
    int getCurrentSampleRate() const;
    int getSampleRate(int i_index) const;
    void setSampleRateByIndex(int i_index);
    std::vector< int > getAvailableSampleRates() const;
    bool isSampleRateAvailable(int i_sampleRate) const;

    virtual void setInputResolution(int i_resolution);
    int getCurrentInputResolution() const;
    int getInputResolution(int i_index) const;
    void setInputResolutionByIndex(int i_index);
    std::vector< int > getAvailableInputResolutions() const;
    bool isInputResolutionAvailable(int i_resolution) const;

    virtual void setOutputResolution(int i_resolution);
    int getCurrentOutputResolution() const;
    int getOutputResolution(int i_index) const;
    void setOutputResolutionByIndex(int i_index);
    std::vector< int > getAvailableOutputResolutions() const;
    bool isOutputResolutionAvailable(int i_resolution) const;

    int getProductId() const;
    int getVendorId() const;
    std::string getDeviceString() const;
};

