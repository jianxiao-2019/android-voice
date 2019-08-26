package ai.kikago.usb;

// TODO: KikaGoDeviceDataSource
public class UsbAudio {
    static {
        System.loadLibrary("usbaudio");
    }


    /**
     * Return result of setup,
     * This function will enumerate and configure usb audio device.
     * After setup(), we should invoke loop() to prepare to receive
     * usb data.
     *
     * @param path usb device path.
     *             fd     usb descriptor handle.
     *             vid    vendor id.
     *             pid    product id.
     * @return true   setup ok
     * false  setup failed
     */
    public native synchronized boolean setup(String path, int fd, int vid, int pid);


    /**
     * Return result of setup,
     * This function will enumerate and configure usb audio device.
     * After setup(), we should invoke loop() to prepare to receive
     * usb data.
     *
     * @param path usb device path.
     *             fd     usb descriptor handle.
     *             vid    vendor id.
     *             pid    product id.
     * @return -1   setup failed
     * 1    mono channel
     * 2    stereo
     */
    public native synchronized int setupWithChannelNo(String path, int fd, int vid, int pid);

    /**
     * Inform device to prepare to capture usb data stream
     * and waiting for start() to invoke callback function :
     * AudioPlayBack.write()
     */
    public native void loop();

    /**
     * Inform device to capture usb data stream once,
     * it will invoke callback function once:
     * AudioPlayBack.write()
     */
    public native void captureOnce();


    /**
     * Close usb handle.
     * After this call, start and stop function become non-sense.
     */
    public native synchronized void close();


    /**
     * Allow device to capture usb data stream, data will be
     * transmit to AudioPlayBack.write() function
     */
    public native synchronized void start();


    /**
     * Disallow capturing usb data stream, then AudioPlayBack.write()
     * function will not be invoked.
     * An invoke of start() will re-allow device to capture usb data
     * stram.
     */
    public native synchronized void stop();

    /**
     * Return 2 bytes of firmware version
     *
     * @return two bytes firmware version, ex. 0x12 0x20
     * 0xFFFF, means fail to get firmware version
     */
    public native byte[] checkFwVersion();

    /**
     * Return 2 bytes of driver version, ex. 0x00 0x01
     *
     * @return two bytes driver version
     */
    public native static byte[] checkDriverVersion();

    /**
     * Return volume level. Max: 9, Min: 1
     * 1              -16.5 db
     * 2              -6.5 db
     * 3              0 db
     * 4              5 db
     * 5              10 db
     * 6              15 db
     * 7              20 db
     * 8              25 db
     * 9              30 db
     *
     * @return 1 ~ 9   volume level
     * 255     error
     */
    public native int checkVolumeState();


    /**
     * Inscrease volume level.
     * Return actuel volume level. Max: 9, Min: 1
     * 1              -16.5 db
     * 2              -6.5 db
     * 3              0 db
     * 4              5 db
     * 5              10 db
     * 6              15 db
     * 7              20 db
     * 8              25 db
     * 9              30 db
     *
     * @return 1 ~ 9   volume level
     * 255     error
     */
    public native int volumeUp();

    /**
     *  Get 12 bits SN.
     * @return     12 bits SN  <br/>
     *             null
     */
    public native String getSn();

    /**
     * Descrease volume level.
     * Return actuel volume level. Max: 9, Min: 1
     * 1              -16.5 db
     * 2              -6.5 db
     * 3              0 db
     * 4              5 db
     * 5              10 db
     * 6              15 db
     * 7              20 db
     * 8              25 db
     * 9              30 db
     *
     * @return 1 ~ 9   volume level
     * 255     error
     */
    public native int volumeDown();

    public native int measure();

}
