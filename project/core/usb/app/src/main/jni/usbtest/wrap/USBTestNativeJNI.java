/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.extreamsd.usbtester;

public class USBTestNativeJNI {
  public final static native String AudioChannel_getName(long jarg1, AudioChannel jarg1_);
  public final static native int AudioChannel_getTracks(long jarg1, AudioChannel jarg1_);
  public final static native int AudioChannel_getTrackOffset(long jarg1, AudioChannel jarg1_);
  public final static native void delete_AudioChannel(long jarg1);
  public final static native long new_AudioChannelVector__SWIG_0();
  public final static native long new_AudioChannelVector__SWIG_1(long jarg1);
  public final static native long AudioChannelVector_size(long jarg1, AudioChannelVector jarg1_);
  public final static native long AudioChannelVector_capacity(long jarg1, AudioChannelVector jarg1_);
  public final static native void AudioChannelVector_reserve(long jarg1, AudioChannelVector jarg1_, long jarg2);
  public final static native boolean AudioChannelVector_isEmpty(long jarg1, AudioChannelVector jarg1_);
  public final static native void AudioChannelVector_clear(long jarg1, AudioChannelVector jarg1_);
  public final static native void AudioChannelVector_add(long jarg1, AudioChannelVector jarg1_, long jarg2, AudioChannel jarg2_);
  public final static native long AudioChannelVector_get(long jarg1, AudioChannelVector jarg1_, int jarg2);
  public final static native void AudioChannelVector_set(long jarg1, AudioChannelVector jarg1_, int jarg2, long jarg3, AudioChannel jarg3_);
  public final static native void delete_AudioChannelVector(long jarg1);
  public final static native int AudioMode_getNumberOfChannels(long jarg1, AudioMode jarg1_);
  public final static native String AudioMode_getName(long jarg1, AudioMode jarg1_);
  public final static native int AudioMode_getNumberOfAudioChannelCombinations(long jarg1, AudioMode jarg1_);
  public final static native long AudioMode_getAudioChannelCombination(long jarg1, AudioMode jarg1_, int jarg2);
  public final static native int AudioMode_getIndexOfAudioChannel(long jarg1, AudioMode jarg1_, long jarg2, AudioChannel jarg2_);
  public final static native long AudioMode_getChannels(long jarg1, AudioMode jarg1_, int jarg2);
  public final static native void delete_AudioMode(long jarg1);
  public final static native long new_IntVector__SWIG_0();
  public final static native long new_IntVector__SWIG_1(long jarg1);
  public final static native long IntVector_size(long jarg1, IntVector jarg1_);
  public final static native long IntVector_capacity(long jarg1, IntVector jarg1_);
  public final static native void IntVector_reserve(long jarg1, IntVector jarg1_, long jarg2);
  public final static native boolean IntVector_isEmpty(long jarg1, IntVector jarg1_);
  public final static native void IntVector_clear(long jarg1, IntVector jarg1_);
  public final static native void IntVector_add(long jarg1, IntVector jarg1_, int jarg2);
  public final static native int IntVector_get(long jarg1, IntVector jarg1_, int jarg2);
  public final static native void IntVector_set(long jarg1, IntVector jarg1_, int jarg2, int jarg3);
  public final static native void delete_IntVector(long jarg1);
  public final static native long AudioDevice_getCurrentOutputMode(long jarg1, AudioDevice jarg1_);
  public final static native void AudioDevice_setCurrentOutputMode(long jarg1, AudioDevice jarg1_, long jarg2, AudioMode jarg2_);
  public final static native int AudioDevice_getNumberOfInputModes(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_getNumberOfOutputModes(long jarg1, AudioDevice jarg1_);
  public final static native void AudioDevice_setLastUsedInputMode(long jarg1, AudioDevice jarg1_, long jarg2, AudioMode jarg2_);
  public final static native long AudioDevice_getInputMode(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native long AudioDevice_getOutputMode(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native String AudioDevice_getName(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_audioInputModeToIndex(long jarg1, AudioDevice jarg1_, long jarg2, AudioMode jarg2_);
  public final static native int AudioDevice_audioOutputModeToIndex(long jarg1, AudioDevice jarg1_, long jarg2, AudioMode jarg2_);
  public final static native boolean AudioDevice_setSampleRate(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native int AudioDevice_getCurrentSampleRate(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_getSampleRate(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native void AudioDevice_setSampleRateByIndex(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native long AudioDevice_getAvailableSampleRates(long jarg1, AudioDevice jarg1_);
  public final static native boolean AudioDevice_isSampleRateAvailable(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native void AudioDevice_setInputResolution(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native int AudioDevice_getCurrentInputResolution(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_getInputResolution(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native void AudioDevice_setInputResolutionByIndex(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native long AudioDevice_getAvailableInputResolutions(long jarg1, AudioDevice jarg1_);
  public final static native boolean AudioDevice_isInputResolutionAvailable(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native void AudioDevice_setOutputResolution(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native int AudioDevice_getCurrentOutputResolution(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_getOutputResolution(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native void AudioDevice_setOutputResolutionByIndex(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native long AudioDevice_getAvailableOutputResolutions(long jarg1, AudioDevice jarg1_);
  public final static native boolean AudioDevice_isOutputResolutionAvailable(long jarg1, AudioDevice jarg1_, int jarg2);
  public final static native int AudioDevice_getProductId(long jarg1, AudioDevice jarg1_);
  public final static native int AudioDevice_getVendorId(long jarg1, AudioDevice jarg1_);
  public final static native String AudioDevice_getDeviceString(long jarg1, AudioDevice jarg1_);
  public final static native void delete_AudioDevice(long jarg1);
  public final static native int IVolumeController_getCurrentVolume(long jarg1, IVolumeController jarg1_, boolean[] jarg2);
  public final static native int IVolumeController_getMinVolume(long jarg1, IVolumeController jarg1_, boolean[] jarg2);
  public final static native int IVolumeController_getMaxVolume(long jarg1, IVolumeController jarg1_, boolean[] jarg2);
  public final static native void IVolumeController_setCurrentVolume(long jarg1, IVolumeController jarg1_, int jarg2);
  public final static native boolean IVolumeController_getMute(long jarg1, IVolumeController jarg1_);
  public final static native void IVolumeController_setMute(long jarg1, IVolumeController jarg1_, boolean jarg2);
  public final static native String IVolumeController_getName(long jarg1, IVolumeController jarg1_);
  public final static native boolean IVolumeController_hasMute(long jarg1, IVolumeController jarg1_);
  public final static native boolean IVolumeController_hasVolume(long jarg1, IVolumeController jarg1_);
  public final static native short IVolumeController_getID(long jarg1, IVolumeController jarg1_);
  public final static native short IVolumeController_getLastVolumeSet(long jarg1, IVolumeController jarg1_);
  public final static native void delete_IVolumeController(long jarg1);
  public final static native void USBSelectorUnit_selectInput(long jarg1, USBSelectorUnit jarg1_, int jarg2);
  public final static native int USBSelectorUnit_getNumberOfInputs(long jarg1, USBSelectorUnit jarg1_);
  public final static native int USBSelectorUnit_getInputNr(long jarg1, USBSelectorUnit jarg1_, int jarg2);
  public final static native void delete_USBSelectorUnit(long jarg1);
  public final static native String USBAudioManager_getOpenDeviceErrorMessage(long jarg1, USBAudioManager jarg1_);
  public final static native boolean USBAudioManager_isAudioDevicePresentForLinux(long jarg1, USBAudioManager jarg1_);
  public final static native boolean USBAudioManager_isMidiOutputDevicePresent(long jarg1, USBAudioManager jarg1_);
  public final static native long USBAudioManager_getCurrentAudioDeviceAsAudioDevice(long jarg1, USBAudioManager jarg1_);
  public final static native boolean USBAudioManager_isAudioDevicePresent(long jarg1, USBAudioManager jarg1_);
  public final static native boolean USBAudioManager_isMidiDevicePresent(long jarg1, USBAudioManager jarg1_);
  public final static native void USBAudioManager_setFirmwareDirectory(long jarg1, USBAudioManager jarg1_, String jarg2);
  public final static native void USBAudioManager_doThreadMagic(long jarg1, USBAudioManager jarg1_, int jarg2);
  public final static native boolean USBAudioManager_isUltraLowLatency(long jarg1, USBAudioManager jarg1_);
  public final static native void delete_USBAudioManager(long jarg1);
  public final static native long new_IVolumeControllerVector__SWIG_0();
  public final static native long new_IVolumeControllerVector__SWIG_1(long jarg1);
  public final static native long IVolumeControllerVector_size(long jarg1, IVolumeControllerVector jarg1_);
  public final static native long IVolumeControllerVector_capacity(long jarg1, IVolumeControllerVector jarg1_);
  public final static native void IVolumeControllerVector_reserve(long jarg1, IVolumeControllerVector jarg1_, long jarg2);
  public final static native boolean IVolumeControllerVector_isEmpty(long jarg1, IVolumeControllerVector jarg1_);
  public final static native void IVolumeControllerVector_clear(long jarg1, IVolumeControllerVector jarg1_);
  public final static native void IVolumeControllerVector_add(long jarg1, IVolumeControllerVector jarg1_, long jarg2, IVolumeController jarg2_);
  public final static native long IVolumeControllerVector_get(long jarg1, IVolumeControllerVector jarg1_, int jarg2);
  public final static native void IVolumeControllerVector_set(long jarg1, IVolumeControllerVector jarg1_, int jarg2, long jarg3, IVolumeController jarg3_);
  public final static native void delete_IVolumeControllerVector(long jarg1);
  public final static native long new_USBSelectorUnitVector__SWIG_0();
  public final static native long new_USBSelectorUnitVector__SWIG_1(long jarg1);
  public final static native long USBSelectorUnitVector_size(long jarg1, USBSelectorUnitVector jarg1_);
  public final static native long USBSelectorUnitVector_capacity(long jarg1, USBSelectorUnitVector jarg1_);
  public final static native void USBSelectorUnitVector_reserve(long jarg1, USBSelectorUnitVector jarg1_, long jarg2);
  public final static native boolean USBSelectorUnitVector_isEmpty(long jarg1, USBSelectorUnitVector jarg1_);
  public final static native void USBSelectorUnitVector_clear(long jarg1, USBSelectorUnitVector jarg1_);
  public final static native void USBSelectorUnitVector_add(long jarg1, USBSelectorUnitVector jarg1_, long jarg2, USBSelectorUnit jarg2_);
  public final static native long USBSelectorUnitVector_get(long jarg1, USBSelectorUnitVector jarg1_, int jarg2);
  public final static native void USBSelectorUnitVector_set(long jarg1, USBSelectorUnitVector jarg1_, int jarg2, long jarg3, USBSelectorUnit jarg3_);
  public final static native void delete_USBSelectorUnitVector(long jarg1);
  public final static native boolean USBAudioDevice_hasAudioInput(long jarg1, USBAudioDevice jarg1_);
  public final static native boolean USBAudioDevice_hasAudioOutput(long jarg1, USBAudioDevice jarg1_);
  public final static native boolean USBAudioDevice_hasMIDIInput(long jarg1, USBAudioDevice jarg1_);
  public final static native boolean USBAudioDevice_hasMIDIOutput(long jarg1, USBAudioDevice jarg1_);
  public final static native boolean USBAudioDevice_isMIDIOnly(long jarg1, USBAudioDevice jarg1_);
  public final static native boolean USBAudioDevice_startAllTransfers(long jarg1, USBAudioDevice jarg1_, boolean jarg2, boolean jarg3, boolean jarg4);
  public final static native int USBAudioDevice_stopAllTransfers(long jarg1, USBAudioDevice jarg1_);
  public final static native long USBAudioDevice_getAllVolumeControllers(long jarg1, USBAudioDevice jarg1_);
  public final static native long USBAudioDevice_getAllSelectorUnits(long jarg1, USBAudioDevice jarg1_);
  public final static native int USBAudioDevice_getVendorID(long jarg1, USBAudioDevice jarg1_);
  public final static native int USBAudioDevice_getProductID(long jarg1, USBAudioDevice jarg1_);
  public final static native String USBAudioDevice_getManufacturerString(long jarg1, USBAudioDevice jarg1_);
  public final static native String USBAudioDevice_getProductString(long jarg1, USBAudioDevice jarg1_);
  public final static native String USBAudioDevice_getSerialNumberString(long jarg1, USBAudioDevice jarg1_);
  public final static native void delete_USBAudioDevice(long jarg1);
  public final static native long new_USBControl();
  public final static native void delete_USBControl(long jarg1);
  public final static native boolean USBControl_initUSB(long jarg1, USBControl jarg1_, boolean jarg2);
  public final static native void USBControl_cleanUp(long jarg1, USBControl jarg1_);
  public final static native void USBControl_restartUSBAudioManager(long jarg1, USBControl jarg1_, boolean jarg2);
  public final static native boolean USBControl_initUSBDevice(long jarg1, USBControl jarg1_, int jarg2, int jarg3, int jarg4);
  public final static native boolean USBControl_initUSBDeviceByName(long jarg1, USBControl jarg1_, int jarg2, String jarg3, int jarg4, int jarg5, byte[] jarg6, int jarg7);
  public final static native String USBControl_getOpenDeviceErrorMessage(long jarg1, USBControl jarg1_);
  public final static native boolean USBControl_isAudioDevicePresentForLinux(long jarg1, USBControl jarg1_);
  public final static native boolean USBControl_startAudioTransfers(long jarg1, USBControl jarg1_, boolean jarg2, boolean jarg3, int jarg4, boolean jarg5, int jarg6, int jarg7);
  public final static native void USBControl_stopAudioTransfers(long jarg1, USBControl jarg1_);
  public final static native boolean USBControl_startMIDIInputTransfers(long jarg1, USBControl jarg1_);
  public final static native void USBControl_stopMIDIInputTransfers(long jarg1, USBControl jarg1_);
  public final static native void USBControl_setUSBSampleRate(long jarg1, USBControl jarg1_, int jarg2);
  public final static native boolean USBControl_startOpenSLES(long jarg1, USBControl jarg1_, boolean jarg2, boolean jarg3, int jarg4, int jarg5, int jarg6);
  public final static native void USBControl_stopOpenSLES(long jarg1, USBControl jarg1_);
  public final static native void USBControl_setObjectToPassArrayTo(long jarg1, USBControl jarg1_, Object jarg2);
  public final static native void USBControl_sendMidiMessage(long jarg1, USBControl jarg1_, int jarg2, int jarg3, int jarg4);
  public final static native void setLogFileName(String jarg1);
  public final static native long USBAudioDevice_SWIGUpcast(long jarg1);
}