package com.kikatech.usb.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * @author SkeeterWang Created on 2017/10/25.
 */
public class AudioUtil
{
	private static final String TAG = "AudioUtil";

	private static final int DEFAULT_PLAY_SAMPLE_RATE = 48000;

	private static AudioUtil sIns;
	private static AudioTrack mPcmAudioTrack;

	public static synchronized AudioUtil getIns()
	{
		if( sIns == null ) {
			sIns = new AudioUtil();
		}
		return sIns;
	}

	private AudioUtil()
	{
		int sampleRate = DEFAULT_PLAY_SAMPLE_RATE;
		int channel = AudioFormat.CHANNEL_OUT_STEREO;
		int format = AudioFormat.ENCODING_PCM_16BIT;
		int bufferSize = AudioTrack.getMinBufferSize( sampleRate,
													  channel,
													  format );
		int mode = AudioTrack.MODE_STREAM;

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
		{
			mPcmAudioTrack = new AudioTrack.Builder()
					.setAudioAttributes( new AudioAttributes.Builder()
												 .setUsage( AudioAttributes.USAGE_MEDIA )
												 .setContentType( AudioAttributes.CONTENT_TYPE_MUSIC )
												 .build())
					.setAudioFormat( new AudioFormat.Builder()
											 .setEncoding( format )
											 .setSampleRate( sampleRate )
											 .setChannelIndexMask( channel )
											 .build())
					.setBufferSizeInBytes( bufferSize )
					.setTransferMode( mode )
					.build();
		}
		else
		{
			mPcmAudioTrack = new AudioTrack( AudioManager.STREAM_MUSIC,
											 sampleRate,
											 channel,
											 format,
											 bufferSize,
											 mode );
		}
		mPcmAudioTrack.play();
	}

	public void playPcm( final short[] data )
	{
		mPcmAudioTrack.write( data, 0, data.length );
	}

	public boolean playSilence( Context context )
	{
		try
		{
			final int sampleRate = DEFAULT_PLAY_SAMPLE_RATE;

			// don't make these final so they can't be used inside the thread
			int bufferSizeBytes = AudioTrack.getMinBufferSize( sampleRate,
															   AudioFormat.CHANNEL_OUT_STEREO,
															   AudioFormat.ENCODING_PCM_16BIT );

			if( bufferSizeBytes <= 0 )
			{
				return false;
			}

			AudioTrack audioTrack;

			if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
			{
				audioTrack = new AudioTrack.Builder()
						.setAudioAttributes(new AudioAttributes.Builder()
													.setUsage( AudioAttributes.USAGE_MEDIA )
													.setContentType( AudioAttributes.CONTENT_TYPE_MUSIC )
													.build() )
						.setAudioFormat(new AudioFormat.Builder()
												.setEncoding( AudioFormat.ENCODING_PCM_16BIT )
												.setSampleRate( sampleRate )
												.setChannelIndexMask( AudioFormat.CHANNEL_OUT_STEREO )
												.build())
						.setBufferSizeInBytes( bufferSizeBytes )
						.setTransferMode( AudioTrack.MODE_STREAM )
						.build();
			}
			else
			{
				audioTrack = new AudioTrack( AudioManager.STREAM_MUSIC,
											 sampleRate,
											 AudioFormat.CHANNEL_OUT_STEREO,
											 AudioFormat.ENCODING_PCM_16BIT,
											 bufferSizeBytes,
											 AudioTrack.MODE_STREAM );
			}

			setAudioTrackOutputToNonUSB( audioTrack, context );

			short samples[] = new short[ bufferSizeBytes / 2 ];
			audioTrack.play();
			audioTrack.write( samples, 0, samples.length );
			audioTrack.write( samples, 0, samples.length );
			audioTrack.write( samples, 0, samples.length );
			audioTrack.write( samples, 0, samples.length );
			audioTrack.write( samples, 0, samples.length );
			audioTrack.stop();
			audioTrack.release();
			return true;
		}
		catch( Exception e )
		{
			if( LogUtil.DEBUG ) LogUtil.printStackTrace( TAG, e.getMessage(), e );
			return false;
		}
	}

	private void setAudioTrackOutputToNonUSB( AudioTrack audioTrack, Context context )
	{
		if( Build.VERSION.SDK_INT >= 23 )
		{
			AudioManager am = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );
			AudioDeviceInfo[] outputs = am.getDevices( AudioManager.GET_DEVICES_OUTPUTS );
			if( outputs != null )
			{
				boolean hasUSBAudioDevice = false;

				for( AudioDeviceInfo audioDeviceInfo : outputs )
				{
					if( audioDeviceInfo != null && audioDeviceInfo.getType() == AudioDeviceInfo.TYPE_USB_DEVICE )
					{
						hasUSBAudioDevice = true;
						break;
					}
				}

				if( hasUSBAudioDevice )
				{
					for( AudioDeviceInfo audioDeviceInfo : outputs )
					{
						if( audioDeviceInfo != null && audioDeviceInfo.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER ) // audioDeviceInfo.getType() != AudioDeviceInfo.TYPE_USB_DEVICE)
						{
							if( LogUtil.DEBUG ) LogUtil.logv( TAG, "Set preferred audio device! type = " + audioDeviceInfo.getType() );
							audioTrack.setPreferredDevice( audioDeviceInfo );
							break;
						}
					}
				}
			}
		}
	}

	//Conversion of short to byte
	public byte[] short2byte( short[] sData )
	{
		int shortArrSize = sData.length;
		byte[] bytes = new byte[ shortArrSize * 2 ];

		for( int i = 0 ; i < shortArrSize ; i++ )
		{
			bytes[ i * 2 ] = ( byte ) ( sData[ i ] & 0x00FF );
			bytes[ ( i * 2 ) + 1 ] = ( byte ) ( sData[ i ] >> 8 );
			sData[ i ] = 0;
		}
		return bytes;
	}
}
