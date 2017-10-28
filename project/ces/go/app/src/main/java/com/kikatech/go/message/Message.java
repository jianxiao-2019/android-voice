package com.kikatech.go.message;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author jasonli Created on 2017/10/19.
 */

public class Message implements Parcelable {

    private String mSender;
    private String mUserName;
    private String mContent;
    private long mTimestamp;

    private Message() {
    }

    public static Message createMessage(long timestamp, String sender, String userName, String content) {
        Message message = new Message();
        message.setTimestamp(timestamp);
        message.setSender(sender);
        message.setUserName(userName);
        message.setContent(content);
        return message;
    }

    public static Message createMessage(long timestamp, String userName, String content) {
        return createMessage(timestamp, null, userName, content);
    }

    public static Message createMessage(String userName, String content) {
        return createMessage(System.currentTimeMillis(), null, userName, content);
    }

    public void setSender(String sender) {
        this.mSender = sender;
    }

    public String getSender() {
        return mSender;
    }

    public void setUserName( String userName ) {
        this.mUserName = userName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setContent(String msgContent ) {
        this.mContent = msgContent;
    }

    public String getContent() {
        return mContent;
    }

    public void setTimestamp(long timeStamp) {
        mTimestamp = timeStamp;
    }

    public long getTimestamp() {
        return mTimestamp;
    }





    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTimestamp);
        dest.writeString(mSender);
        dest.writeString(mUserName);
        dest.writeString(mContent);
    }

    private Message(Parcel source) {
        mTimestamp = source.readLong();
        mSender = source.readString();
        mUserName = source.readString();
        mContent = source.readString();
    }

    public static final Creator< Message > CREATOR = new Creator< Message >()
    {
        @Override
        public Message createFromParcel( Parcel source )
        {
            return new Message( source );
        }

        @Override
        public Message[] newArray( int size )
        {
            return new Message[ size ];
        }
    };

}
