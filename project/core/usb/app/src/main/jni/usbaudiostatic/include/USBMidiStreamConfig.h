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
 * \brief Utility class, combining an alt setting and an endpoint for MIDI. Basically, one selects a USBMidiStreamConfig for
 *        the current MIDI input and output of a device.
 *        This class allocates buffers for MIDI streaming, creates isochronous transfers and submits them.
 */
class USBMidiStreamConfig
{
public:
	USBMidiStreamConfig(USBAltSetting *i_altSetting,
                        USBEndPoint *i_endPoint,
                        USBAudioDevice* i_parentDevice);
	~USBMidiStreamConfig();

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
    bool submitNextOutputTransfer(unsigned char i_status,
                                  unsigned char i_data1,
                                  unsigned char i_data2);
    bool submitNextOutputTransfer(const std::vector<unsigned char>& i_data);

    void cancelTransfers();
    void deactivateTransfer(struct libusb_transfer *i_transfer);

    void flushBuffer();

    bool isRunning() const;

    unsigned char* readBuffer;
    unsigned char* read;
    int getReadBufferSize() const;
    int readBufferUsed;
    bool m_requestToStop;

private:
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

    unsigned short m_midiInInterfaceClaimed;
    unsigned short m_midiOutInterfaceClaimed;

    bool m_running;
};