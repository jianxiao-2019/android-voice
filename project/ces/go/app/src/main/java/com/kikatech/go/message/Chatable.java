package com.kikatech.go.message;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jasonli Created on 2017/10/20.
 */

public abstract class Chatable  implements Parcelable {

    // Content data
    protected String id = null;
    protected String groupName = null;
    protected String userName = null;
    protected String msgContent;

    protected List< Message > latestMessages = new ArrayList<>();

    public void setId( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setGroupName( String groupName )
    {
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getTitle() {
        if( groupName != null )
            return groupName;
        else
            return userName;
    }

    public void setMsgContent( String msgContent )
    {
        this.msgContent = msgContent;
    }

    public String getMsgContent()
    {
        return msgContent;
    }

    public long getLatestTimestamp()
    {
        Message latestMessage = getLatestMessage();
        if( latestMessage != null ) return 0;
        return latestMessage.getTimestamp();
    }

    public synchronized void setLatestMessages( List< Message > latestMessages )
    {
        this.latestMessages.clear();
        if( latestMessages != null ) this.latestMessages.addAll( latestMessages );
    }

    public synchronized List< Message > getLatestMessages()
    {
        return latestMessages;
    }

    public synchronized Message getLatestMessage() {
        int messageSize = latestMessages.size();
        return messageSize > 0 ? latestMessages.get(messageSize - 1) : null;
    }

    public synchronized void addToLatestMessages( Message msg )
    {
        latestMessages.add( msg );
    }

    public synchronized void deleteMessage( Message talkMessage )
    {
        if( latestMessages != null ) latestMessages.remove( talkMessage );
    }

}
