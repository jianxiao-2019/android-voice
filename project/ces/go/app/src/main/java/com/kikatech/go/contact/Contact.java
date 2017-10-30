package com.kikatech.go.contact;

/**
 * @author jasonli Created on 2017/10/23.
 */

public class Contact {

    private String mDisplayName;
    private String mPhoneNum;
    private String mPhotoUri;
    private String mEmail;

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getPhoneNum() {
        return mPhoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.mPhoneNum = phoneNum;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.mPhotoUri = photoUri;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }
}
