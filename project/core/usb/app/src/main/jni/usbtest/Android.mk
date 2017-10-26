LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := usbtestnative
LOCAL_SRC_FILES := \
    AndroidDebugLogger.cpp \
    InputMonitorBuffer.cpp \
    LogFileLogger.cpp \
    MailBox.cpp \
    OpenSLStream.cpp \
    StringUtils.cpp \
    ToJava.cpp \
    USBControl.cpp \
    wrap/USBTestNative_wrap.cpp

LOCAL_C_INCLUDES :=  $(LOCAL_PATH)/..
#LOCAL_CFLAGS := -DMIDI_SUPPORT
LOCAL_STATIC_LIBRARIES := usbaudiostatic cpufeatures
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog -lz -lOpenSLES
LOCAL_SHARED_LIBRARIES := esdusb

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
