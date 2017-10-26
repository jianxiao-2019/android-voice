/* Copyright (C) 2014 by eXtream Software Development - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 * Written by Davy Wentzler <info@audio-evolution.com>
 */

#pragma once

#include <vector>

class USBAltSetting;
class USBAudioDevice;
class USBEndPoint;

struct libusb_transfer;

/**
* \brief Callback interface for playback.
*
* \param i_ID Unit/Terminal ID that changed its value
* \param i_userData any user data that was passed with setInterruptCallback()
*/
typedef void (*InterruptCallbackFunction)(int i_ID, void *i_userData);


/**
 * \brief Utility class, combining an alt setting and an endpoint for interrupts. 
 *        This class allocates buffers for interrupt transfers and can submit them.
 */
class USBInterruptConfig
{
public:
	USBInterruptConfig(USBAltSetting *i_altSetting,
                       USBEndPoint *i_endPoint,
                       USBAudioDevice* i_parentDevice);
	~USBInterruptConfig();

    USBAltSetting *getUSBAltSetting() const;
    USBEndPoint *getUSBEndPoint() const;
    USBAudioDevice* getUSBAudioDevice() const;

    void allocateBuffers(int i_nrOfTransfers);
    
    bool prepareForOutput(int i_nrOfTransfers);
    bool prepareForInput(int i_nrOfTransfers);
    void fillTransferForInput(unsigned int i_transferIndex);
    void fillTransferForOutput(unsigned int i_transferIndex);

    struct libusb_transfer *getTransfer(unsigned int i_index) const;
    unsigned int getNumberOfTransfers() const;
    unsigned char *getBufferPointer(unsigned int i_index) const;
    unsigned char *getCurrentBufferPointer() const;

    bool startTransfer();
    bool submitNextInputTransfer();
    void cancelTransfers();
    void deactivateTransfer(struct libusb_transfer *i_transfer);

    void setInterruptCallback(InterruptCallbackFunction i_callback,
                              void *i_userData);

private:
    static void inputTransferCallback(struct libusb_transfer *i_transfer);
    static void outputTransferCallback(struct libusb_transfer *i_transfer);
    bool waitForTransferToBecomeInactive(unsigned int i_index);
    void freeBuffers();

    USBAltSetting *m_altSetting;
    USBEndPoint *m_endPoint;
    USBAudioDevice* m_parentDevice;

    std::vector< unsigned char * > m_buffers;
    unsigned int m_bufferSizeBytes;
    int m_currentTransferIndex;

    std::vector< libusb_transfer * > m_transfers;
    std::vector< int > m_transferActive;

    unsigned short m_interruptInInterfaceClaimed;
    unsigned short m_interruptOutInterfaceClaimed;

    bool m_requestToStop;

    InterruptCallbackFunction m_interruptCallback;
    void *m_userData;
};