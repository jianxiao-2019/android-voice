/* Copyright (C) 2014 by eXtream Software Development - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 * Written by Davy Wentzler <info@audio-evolution.com>
 */

#pragma once

#include <string>
#include <pthread.h>
#include <sys/timeb.h>
#include <libusb/libusb.h>
#include <vector>

#ifdef MIDI_SUPPORT
#include <porttime.h>
#endif


struct libusb_config_descriptor;
struct libusb_context;
struct libusb_device;
struct libusb_interface_descriptor;
struct libusb_pollfd;

class AudioDevice;
class CPUBooster;

#ifdef SUPPORTS_TRACK
class AEMAudioDevice;
#endif
#include "USBAudioDevice.h"

/**
 * \brief Wrapper around libusb: opens and closes libusb and libusb audio devices. Finds and opens a libusb device
 *        given a file handle (from Java), product and vendor ID.
 *        Contains the high-priority event thread that handles interrupts from the libusb device and calls into the
 *        callbacks implemented in USBAudioDevice.
 *        
 */
class USBAudioManager
{
public:
    /**
     * \brief USBAudioManager constructor
     * \param i_libraryDirectory Internal use, you can pass an empty string
     * \param i_nativeSampleRate Not used
     * \param i_nativeBufferSize Not used
     */
    USBAudioManager(const std::string& i_libraryDirectory, int i_nativeSampleRate, int i_nativeBufferSize, bool i_tryRealTimeThread);
    ~USBAudioManager();

    bool init(bool i_enumerateDevices);

    /**
     * \brief Opens the file descriptor (hopefully) associated with a USB audio/MIDI class device.
     *
     * \param o_askResetDevice If this is true after calling the function, there were problems initializing the device. Basically, this is a fatal error.
     * \param i_disableHID When true, will disable any HID interface by calling libusb_detach_kernel_driver() on the HID interface
     * \param o_stackTrace Internal use
     * \param i_findInputs Set to true if you want to use USB audio inputs for recording
     * \param i_findMidiInputs Set to true if you want to enable MIDI inputs
     * \param i_findMidiOutputs Set to true if you want to enable MIDI outputs
     * \param i_findInterruptInputs Set to true if you want to enable interrupt transfers for interrupt input endpoints of the AudioControl interface 0
     * \param i_rawDescriptors Raw USB descriptors. Needed on Android 7 and higher
     * \param i_rawDescriptionsLength Raw USB descriptors length. Needed on Android 7 and higher
     * \param i_busSpeedEstimation USB bus speed. On Android 7 and higher, the bus speed cannot be determined. Passing Automatic usually gives the correct results though.
     */
    USBAudioDevice *OpenDeviceFD(int i_fd, int i_productId, int i_vendorId, bool *o_askResetDevice, bool i_disableHID, int& o_stackTrace,
                                 bool i_findInputs, bool i_findMidiInputs, bool i_findMidiOutputs, bool i_findInterruptInputs);
    USBAudioDevice *OpenDeviceFDAndName(int i_fd, std::string i_deviceName, int i_productId, int i_vendorId, bool *o_askResetDevice, bool i_disableHID, int& o_stackTrace,
                                        bool i_findInputs, bool i_findMidiInputs, bool i_findMidiOutputs, bool i_findInterruptInputs, unsigned char* i_rawDescriptors, int i_rawDescriptionsLength,
                                        USBAudioDevice::BusSpeedEstimation i_busSpeedEstimation);
    bool resetDevice(int i_fd, int i_productId, int i_vendorId);

    static const char *getClassCodeString(int i_index);
    std::string getOpenDeviceErrorMessage() const;

    // called from static method
    bool addIRQ(const struct libusb_pollfd *i_pollFd);
    bool removeIRQ(int fd);

    libusb_context *getContext();
    int getEpollFD();

    USBAudioDevice *getCurrentAudioDevice() const;
#ifdef SUPPORTS_TRACK
    AEMAudioDevice *getCurrentAudioDeviceAsAudioDevice() const;
#else
    AudioDevice *getCurrentAudioDeviceAsAudioDevice() const;
#endif
    std::vector<USBAudioDevice *> getAllMIDICapableDevices() const;
    std::vector<USBAudioDevice *> getAllDevices() const;
    void removeDevice(USBAudioDevice * i_device);
    bool isAudioDevicePresentForLinux() const;
    bool isMidiOutputDevicePresent() const;

    bool isAudioDevicePresent() const;
    bool isMidiDevicePresent() const;

#ifdef MIDI_SUPPORT
    PtTimestamp getMidiTime() const;
#endif

    void setFirmwareDirectory(std::string i_firmwareDirectory);

    // for rooted devices, change the scheduler s = scheduler
    void doThreadMagic(int s);

    /// Only for internal experimentation
    bool isUltraLowLatency() const;

    void startCPUBoost();
    void stopCPUBoost();

private:
    libusb_device *FindAudioDevice(libusb_device **list, ssize_t nrOfDevices, int i_productId, int i_vendorId) const;
    libusb_device *FindDevice(unsigned short productId, unsigned short vendorId, libusb_device **list, ssize_t nrOfDevices);
    void DumpAllDevices(libusb_device **list, ssize_t nrOfDevices);
    int FindInterface(struct libusb_config_descriptor *i_configuration,
                      unsigned short i_interfaceClass,
                      unsigned short i_interfaceSubClass,
                      bool i_atLeastOneEndPoint) const;
    bool IsAudioClass(libusb_device *i_device, int i_productId, int i_vendorId) const;
    bool IsMIDIClass(libusb_device *i_device) const;
    static int hotplug_callback(struct libusb_context *ctx, struct libusb_device *dev, libusb_hotplug_event event, void *user_data);

    // returns new product ID
    int checkFirmware(libusb_device *device, int i_productId, int i_vendorId, int i_fd);

    bool getPollFds();
    void cleanUpContext();

    bool createEventThread();
    static void *eventThread(void *data);
    bool prepareEventThread();
    static bool s_killEventThread; 

    libusb_context *m_context;

    std::vector< USBAudioDevice * > m_audioDevices;

    // this attribute is a kind of super interrupt, containing sub-IRQ's
    int m_epollFD;

    pthread_t m_eventThread;
    pid_t m_eventThreadTID;

    struct timeval m_midiTimeOffset;

    // directory to load firmware files from
    std::string m_firmwareDirectory;

    std::string m_libraryDirectory;

    libusb_hotplug_callback_handle m_hotPlugHandle;

    bool m_ultraLowLatency;
    int m_nativeSampleRate;
    int m_nativeBufferSize;

    CPUBooster *m_CPUBooster;
    int m_eventThreadCPU;
};