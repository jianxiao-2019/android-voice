/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.extreamsd.usbtester;

public class USBControl {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected USBControl(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(USBControl obj ) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        USBTestNativeJNI.delete_USBControl( swigCPtr );
      }
      swigCPtr = 0;
    }
  }

  public USBControl() {
    this( USBTestNativeJNI.new_USBControl(), true );
  }

  public boolean initUSB(boolean i_enumerateDevices) {
    return USBTestNativeJNI.USBControl_initUSB( swigCPtr, this, i_enumerateDevices );
  }

  public void cleanUp() {
    USBTestNativeJNI.USBControl_cleanUp( swigCPtr, this );
  }

  public void restartUSBAudioManager(boolean i_enumerateDevices) {
    USBTestNativeJNI.USBControl_restartUSBAudioManager( swigCPtr, this, i_enumerateDevices );
  }

  public boolean initUSBDevice(int i_deviceFd, int i_productId, int i_vendorId) {
    return USBTestNativeJNI.USBControl_initUSBDevice( swigCPtr, this, i_deviceFd, i_productId, i_vendorId );
  }

  public boolean initUSBDeviceByName(int i_deviceFd, String i_deviceName, int i_productId, int i_vendorId, byte[] i_rawDescriptors, int i_rawDescriptionsLength) {
    return USBTestNativeJNI.USBControl_initUSBDeviceByName( swigCPtr, this, i_deviceFd, i_deviceName, i_productId, i_vendorId, i_rawDescriptors, i_rawDescriptionsLength );
  }

  public String getOpenDeviceErrorMessage() {
    return USBTestNativeJNI.USBControl_getOpenDeviceErrorMessage( swigCPtr, this );
  }

  public boolean isAudioDevicePresentForLinux() {
    return USBTestNativeJNI.USBControl_isAudioDevicePresentForLinux( swigCPtr, this );
  }

  public boolean startAudioTransfers(boolean i_playAudio, boolean i_recordAudio, int i_sampleRate, boolean i_force1PPT, int i_bufferSizeInFrames, int i_openSLESBufferSizeInFrames) {
    return USBTestNativeJNI.USBControl_startAudioTransfers( swigCPtr, this, i_playAudio, i_recordAudio, i_sampleRate, i_force1PPT, i_bufferSizeInFrames, i_openSLESBufferSizeInFrames );
  }

  public void stopAudioTransfers() {
    USBTestNativeJNI.USBControl_stopAudioTransfers( swigCPtr, this );
  }

  public boolean startMIDIInputTransfers() {
    return USBTestNativeJNI.USBControl_startMIDIInputTransfers( swigCPtr, this );
  }

  public void stopMIDIInputTransfers() {
    USBTestNativeJNI.USBControl_stopMIDIInputTransfers( swigCPtr, this );
  }

  public void setUSBSampleRate(int i_sampleRate) {
    USBTestNativeJNI.USBControl_setUSBSampleRate( swigCPtr, this, i_sampleRate );
  }

  public boolean startOpenSLES(boolean i_play, boolean i_record, int i_actualChannelsRecording, int i_sampleRate, int i_bufferSizeInFrames) {
    return USBTestNativeJNI.USBControl_startOpenSLES( swigCPtr, this, i_play, i_record, i_actualChannelsRecording, i_sampleRate, i_bufferSizeInFrames );
  }

  public void stopOpenSLES() {
    USBTestNativeJNI.USBControl_stopOpenSLES( swigCPtr, this );
  }

  public void setObjectToPassArrayTo(Object i_mainActivity) {
    USBTestNativeJNI.USBControl_setObjectToPassArrayTo( swigCPtr, this, i_mainActivity );
  }

  public void sendMidiMessage(int i_status, int i_byte1, int i_byte2) {
    USBTestNativeJNI.USBControl_sendMidiMessage( swigCPtr, this, i_status, i_byte1, i_byte2 );
  }

}
