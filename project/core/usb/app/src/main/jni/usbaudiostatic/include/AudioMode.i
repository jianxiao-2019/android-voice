%include "std_string.i"
%include <AudioChannel.i>
%include "std_vector.i"

namespace std {
   %template(AudioChannelVector) vector< AudioChannel* >;
};

%nodefaultctor AudioMode;
class AudioMode
{
public:
    int getNumberOfChannels() const;
    std::string getName() const;
    int getNumberOfAudioChannelCombinations() const;
    AudioChannel* getAudioChannelCombination(int i_index);
    int getIndexOfAudioChannel(const AudioChannel* i_audioChannel) const;
    std::vector< AudioChannel* > getChannels(int i_exactTrackCount) const;
};

