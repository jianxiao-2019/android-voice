package com.kikatech.go.ui;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * @author SkeeterWang Created on 2017/11/2.
 */
public class KikaMultiDexApplication extends MultiDexApplication
{
	@Override
	protected void attachBaseContext( Context base )
	{
		super.attachBaseContext( base );
		MultiDex.install( this ); // install multidex
	}
}
