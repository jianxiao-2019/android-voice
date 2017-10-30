package com.kikatech.go.notification;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.RemoteInput;

import java.util.ArrayList;

/**
 * @author wangskeeter Created on 2017/1/19.
 */

public class ParcelableRemoteInput implements Parcelable
{
    private final String mResultKey;
    private final String mLabel;
    private final ArrayList<String> mChoices = new ArrayList<>();
    private final boolean mAllowFreeFormInput;
    private final Bundle mExtras;

    public ParcelableRemoteInput(RemoteInput remoteInput )
    {
        this.mResultKey = remoteInput.getResultKey();
        this.mLabel = remoteInput.getLabel().toString();
        CharSequence[] choices = remoteInput.getChoices();
        if( choices != null )
            for( CharSequence choice : choices )
                this.mChoices.add( choice.toString() );
        this.mAllowFreeFormInput = remoteInput.getAllowFreeFormInput();
        this.mExtras = remoteInput.getExtras();
    }

    public RemoteInput toRemoteInput()
    {
        RemoteInput.Builder remoteInputBuilder = new RemoteInput.Builder( mResultKey );

        remoteInputBuilder.setLabel( mLabel );
        remoteInputBuilder.setAllowFreeFormInput( mAllowFreeFormInput );
        remoteInputBuilder.addExtras( mExtras );

        if( mChoices != null && !mChoices.isEmpty() )
        {
            CharSequence[] choices = new CharSequence[ mChoices.size() ];
            int i = 0;
            for( String choice : mChoices )
                choices[ i++ ] = choice;
            remoteInputBuilder.setChoices( choices );
        }

        return remoteInputBuilder.build();
    }

    public String getResultKey()
	{
		return mResultKey;
	}


    protected ParcelableRemoteInput(Parcel in )
    {
        mResultKey = in.readString();
        mLabel = in.readString();
        in.readList( mChoices, String.class.getClassLoader() );
        mAllowFreeFormInput = in.readByte() != 0;
        mExtras = in.readBundle( getClass().getClassLoader() );
    }

    public static final Creator< ParcelableRemoteInput > CREATOR = new Creator< ParcelableRemoteInput >()
    {
        @Override
        public ParcelableRemoteInput createFromParcel( Parcel in )
        {
            return new ParcelableRemoteInput( in );
        }

        @Override
        public ParcelableRemoteInput[] newArray( int size )
        {
            return new ParcelableRemoteInput[ size ];
        }
    };

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel dest, int flags )
    {
        dest.writeString( mResultKey );
        dest.writeString( mLabel );
        dest.writeList( mChoices );
        dest.writeByte( ( byte ) ( mAllowFreeFormInput ? 1 : 0 ) );
        dest.writeBundle( mExtras );
    }
}
