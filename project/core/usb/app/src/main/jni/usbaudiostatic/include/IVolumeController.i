%include "std_string.i"
%include "typemaps.i"

%nodefaultctor IVolumeController;
class IVolumeController
{
public:
    virtual int getCurrentVolume(bool *OUTPUT) const = 0;
    virtual int getMinVolume(bool *OUTPUT) const = 0;
    virtual int getMaxVolume(bool *OUTPUT) const = 0;
    virtual void setCurrentVolume(int i_volume) = 0;

    virtual bool getMute() = 0;
    virtual void setMute(bool i_on) = 0;

    std::string getName();

    virtual bool hasMute() const;
    virtual bool hasVolume() const;

    short getID() const;

    short getLastVolumeSet() const;
};

