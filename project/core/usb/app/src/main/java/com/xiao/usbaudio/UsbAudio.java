package com.xiao.usbaudio;

public class UsbAudio {
    static {
        System.loadLibrary("usbaudio");
    }

    public native boolean setup(String path, int fd, int vid, int pid);
    public native void close();
    public native void loop();
    public native boolean stop();
    public native int measure();

}
