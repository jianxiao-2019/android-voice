package com.kikatech.go.message.sms;

import android.os.Parcel;
import android.os.Parcelable;

import com.kikatech.go.message.Chatable;
import com.kikatech.go.message.Message;

/**
 * @author jasonli Created on 2017/10/20.
 */

public class SmsObject extends Chatable {

    private String address;     // the phone address
    private String photoUri;

    public SmsObject() {

    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getPhotoUri() {
        return photoUri;
    }


    /**
     * Parcelable interfaces
     **/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userName);
        dest.writeString(photoUri);
        dest.writeString(address);
        dest.writeString(msgContent);
        dest.writeTypedList(latestMessages);
    }

    private SmsObject(Parcel source) {
        id = source.readString();
        userName = source.readString();
        photoUri = source.readString();
        address = source.readString();
        msgContent = source.readString();
        latestMessages = source.createTypedArrayList(Message.CREATOR);
    }

    public static final Parcelable.Creator<SmsObject> CREATOR = new Parcelable.Creator<SmsObject>() {
        @Override
        public SmsObject createFromParcel(Parcel source) {
            // reading values ordered by writeToParcel()
            return new SmsObject(source);
        }

        @Override
        public SmsObject[] newArray(int size) {
            return new SmsObject[size];
        }
    };
}
