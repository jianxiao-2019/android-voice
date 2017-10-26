%include <std_string.i>

%nodefaultctor AudioChannel;
class AudioChannel
{
public:
    std::string getName() const;
    int getTracks() const;
    int getTrackOffset() const;
};

