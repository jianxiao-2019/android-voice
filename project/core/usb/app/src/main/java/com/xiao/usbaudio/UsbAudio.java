package com.xiao.usbaudio;

public class UsbAudio {
    static {
        System.loadLibrary("usbaudio");
    }


    /**
     *  Return result of setup,
     *  This function will enumerate and configure usb audio device.
     *  After setup(), we should invoke loop() to prepare to receive
     *  usb data.
     * @param      path   usb device path.
     *             fd     usb descriptor handle.
     *             vid    vendor id.
     *             pid    product id.
     *
     * @return     true   setup ok
     *             false  setup failed
     */
    public native boolean setup(String path, int fd, int vid, int pid);


    /**
     *  Inform device to prepare to capture usb data stream
     *  and waiting for start() to invoke callback function :
     *  AudioPlayBack.write()
     */
    public native void loop();


    /**
     *  Close usb handle.
     *  After this call, start and stop function become non-sense.
     */
    public native void close();


    /**
     *  Allow device to capture usb data stream, data will be
     *  transmit to AudioPlayBack.write() function
     */
    public native void start();


    /**
     *  Disallow capturing usb data stream, then AudioPlayBack.write()
     *  function will not be invoked.
     *  An invoke of start() will re-allow device to capture usb data
     *  stram.
     */
    public native void stop();


    public native int measure();

}
