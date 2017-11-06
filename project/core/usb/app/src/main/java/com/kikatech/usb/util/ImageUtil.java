package com.kikatech.usb.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */
public class ImageUtil {
    public static Bitmap safeDecodeFile(String path) {
        Bitmap ret = null;
        //Bitmap.Config.RGB_565
        try {
            ret = BitmapFactory.decodeFile(path);
        } catch (OutOfMemoryError oom) {
            System.gc();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                ret = BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError ignore) {
                // Try to stretch ?
            }
        }
        return ret;
    }

    public static Bitmap safeDecodeFile(Resources res, int id) {
        Bitmap ret = null;
        try {
            ret = BitmapFactory.decodeResource(res, id);
        } catch (OutOfMemoryError oom) {
            System.gc();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                ret = BitmapFactory.decodeResource(res, id, options);
            } catch (OutOfMemoryError ignore) {
                // Try to stretch ?
            }
        }
        return ret;
    }
}
