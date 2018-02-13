package com.kikatech.voice.core.debug;

import com.kikatech.voice.util.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ryanlin on 04/01/2018.
 */

public class WavHeaderHelper {

    public static void addWavHeader(File file, boolean isMono) {
        try {
            byte[] bytesArray = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            int readSize = fis.read(bytesArray); //read file into bytes[]
            Logger.d("addWavHeader readSize = " + readSize);
            fis.close();

            FileOutputStream fos = new FileOutputStream(file.getPath() + ".wav");
            fos.write(addHeader(bytesArray, 16000, 16, isMono ? 1 : 2));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] addHeader(byte[] pcm, int sampleRate, int bitsPerSample, int channel) {
        byte[] header = new byte[44];

        long totalDataLen = pcm.length + 36;
        long bitrate = sampleRate * channel * bitsPerSample;
        long dataLen = pcm.length;

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = (byte) 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channel;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * bitsPerSample) / 8);
        header[33] = 0;
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (dataLen & 0xff);
        header[41] = (byte) ((dataLen >> 8) & 0xff);
        header[42] = (byte) ((dataLen >> 16) & 0xff);
        header[43] = (byte) ((dataLen >> 24) & 0xff);

        byte[] result = new byte[header.length + pcm.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(pcm, 0, result, header.length, pcm.length);

        return result;
    }
}
