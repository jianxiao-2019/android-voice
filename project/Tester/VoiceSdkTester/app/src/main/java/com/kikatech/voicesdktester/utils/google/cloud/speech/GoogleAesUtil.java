package com.kikatech.voicesdktester.utils.google.cloud.speech;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.google.common.base.Strings;
import com.kikatech.voice.util.log.Logger;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author SkeeterWang Created on 2018/6/21.
 */

public class GoogleAesUtil {
    private static final String TAG = "GoogleAesUtil";

    private static final String AES_ECB_PKCS5_PADDING = "AES/ECB/PKCS5Padding";
    private static final String AES = "AES";
    private static final String UTF_8 = "UTF-8";
    private static final String SHA_1 = "SHA-1";

    private static final String DEFAULT_SECRET = "X3Tu84KCtyanzWXGJlDZ8DO9";
    private static final SecretKeySpec DEFAULT_KEY = generateKey(DEFAULT_SECRET);

    private static final int FLAG = Base64.NO_WRAP;

    private static SecretKeySpec generateKey(String secret) {
        try {
            byte[] key = secret.getBytes(UTF_8);
            MessageDigest sha = MessageDigest.getInstance(SHA_1);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, AES);
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressLint("GetInstance")
    public static String encrypt(String raw) {
        if (Strings.isNullOrEmpty(raw)) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, DEFAULT_KEY);
            return new String(Base64.encode(cipher.doFinal(raw.getBytes(UTF_8)), FLAG));
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressLint("GetInstance")
    public static String encrypt(String raw, String secret) {
        if (Strings.isNullOrEmpty(raw)) {
            return "";
        }
        try {
            SecretKeySpec key = generateKey(secret);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return new String(cipher.doFinal(raw.getBytes(UTF_8)));
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressLint("GetInstance")
    public static String decrypt(String encrypted) {
        if (Strings.isNullOrEmpty(encrypted)) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, DEFAULT_KEY);
            if (Logger.DEBUG) {
                Logger.d(TAG, String.format("encrypted.length(): %s", encrypted.length()));
            }
            return new String(cipher.doFinal(Base64.decode(encrypted, FLAG)));
//            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    @SuppressLint("GetInstance")
    public static String decrypt(String encrypted, String secret) {
        if (Strings.isNullOrEmpty(encrypted)) {
            return "";
        }
        try {
            SecretKeySpec key = generateKey(secret);
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(encrypted.getBytes(UTF_8)));
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.printStackTrace(TAG, e.getMessage(), e);
            }
        }
        return null;
    }
}
