%module USBTestNative
%{
    #include "../usbaudiostatic/include/AudioChannel.h"
    #include "../usbaudiostatic/include/AudioDevice.h"
    #include "../usbaudiostatic/include/AudioMode.h"
    #include "../usbaudiostatic/include/IVolumeController.h"
    #include "../usbaudiostatic/include/USBAudioDevice.h"
    #include "../usbaudiostatic/include/USBAudioManager.h"
    #include "../usbaudiostatic/include/USBSelectorUnit.h"
    #include "USBControl.h"
    #include "ToJava.h"
%}

%include "../usbaudiostatic/include/AudioChannel.i"
%include "../usbaudiostatic/include/AudioDevice.i"
%include "../usbaudiostatic/include/AudioMode.i"
%include "../usbaudiostatic/include/IVolumeController.i"
%include "../usbaudiostatic/include/USBAudioDevice.i"
%include "../usbaudiostatic/include/USBAudioManager.i"
%include "../usbaudiostatic/include/USBSelectorUnit.i"
%include USBControl.i

%include ToJava.i