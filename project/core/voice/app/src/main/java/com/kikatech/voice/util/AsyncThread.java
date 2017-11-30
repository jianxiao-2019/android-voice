package com.kikatech.voice.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wangskeeter Created on 16/8/24.
 */
public class AsyncThread
{
    private static AsyncThread sIns;
    private ScheduledThreadPoolExecutor mExecutor;

    public static synchronized AsyncThread getIns()
    {
        if( sIns == null )
            sIns = new AsyncThread();
        return sIns;
    }

    private AsyncThread()
    {
        mExecutor = new ScheduledThreadPoolExecutor( 2, new ScheduledThreadPoolExecutor.DiscardOldestPolicy() );
    }

    public void execute( Runnable runnable )
    {
        mExecutor.execute( runnable );
    }

    public void executeDelay( Runnable runnable , long delay )
    {
        mExecutor.schedule( runnable , delay , TimeUnit.MILLISECONDS );
    }

    public void remove( Runnable runnable )
    {
        mExecutor.remove( runnable );
    }
}
