/* Copyright (C) 2014 by eXtream Software Development - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 * Written by Davy Wentzler <info@audio-evolution.com>
 */

#pragma once
#include <libusb/libusb.h>
#include <string>

#define SET_CUR 0x01
#define DIR_GET 0x80
#define GET_CUR 0x81
#define GET_MIN 0x82
#define GET_MAX 0x83
#define GET_RES 0x84

/**
 *  \brief Interface class wrapping USB feature and mixer units to control volume/gain and mute.
 */
class IVolumeController
{
public:
    IVolumeController(const std::string& i_name,
                      bool i_hasMute,
                      bool i_hasVolume,
                      short i_ID)
    {
        m_name = i_name; m_hasMute = i_hasMute; m_hasVolume = i_hasVolume; m_ID = i_ID; m_lastVolumeSet = 0;
    }
    virtual ~IVolumeController() {};

    virtual short getCurrentVolume(bool *o_valid) const = 0;
    virtual short getMinVolume(bool *o_valid) const = 0;
    virtual short getMaxVolume(bool *o_valid) const = 0;
    virtual void setCurrentVolume(short i_volume) = 0;

    virtual bool getMute() = 0;
    virtual void setMute(bool i_on) = 0;

    std::string getName() { return m_name; }

    bool hasMute() const { return m_hasMute; }
    bool hasVolume() const  { return m_hasVolume; }

    // unit ID
    short getID() const { return m_ID; }

    short getLastVolumeSet() const { return m_lastVolumeSet; }

protected:
    std::string m_name;
    bool m_hasMute;
    bool m_hasVolume;
    short m_ID;

    // The LG B&O DAC for example has a bug where it fails to read the volume, but it can be set.
    // This value is updated to the last volume that is set and could be read when 'valid' is false. 
    short m_lastVolumeSet;
};

