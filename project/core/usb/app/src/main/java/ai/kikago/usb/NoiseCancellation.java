package ai.kikago.usb;

public class NoiseCancellation {
    public NoiseCancellation() {
    }

    public static native int NoiseMask0(short[] var0, short[] var1);
    public static native int NoiseMask180(short[] var0, short[] var1);

    public static native int Init();

    public static native void Destroy();

    public static native int GetVersion();

    public static native void SetControl(int var0, int var1);

    public static native int GetControl(int var0);

    public static native void SetRefGain(float var0);

    public static native float GetRefGain();

    public static native void enableWebrtc();

    public static native void enableBeamforming();

    public static native void Omlsa();

    public static native void enableNoiseGate();

    public static native void enableEq();

    public static native void enableAgc();

    public static native void Gain();

    static {
        System.loadLibrary("kikago_nc");
    }

}
