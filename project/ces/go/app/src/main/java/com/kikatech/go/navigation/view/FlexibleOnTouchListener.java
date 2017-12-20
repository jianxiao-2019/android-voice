package com.kikatech.go.navigation.view;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.kikatech.go.util.timer.CountingTimer;


/**
 * @author SkeeterWang Created on 2017/4/17.
 */
public class FlexibleOnTouchListener implements View.OnTouchListener
{
	private static final String TAG = "FlexibleOnTouchListener";

	private final long SHORT_PRESS_TRIGGER_TIME;

	private boolean viewTouched;
	private long eventStartTime = 0;
	private GestureDetector mGestureDetector;
	private CountingTimer timer;
	private ITouchListener mTouchListener;

	private boolean interceptEvent = true;

	public FlexibleOnTouchListener( long shortPressTriggerTime, ITouchListener listener )
	{
		this.SHORT_PRESS_TRIGGER_TIME = shortPressTriggerTime;
		this.mTouchListener = listener;
	}

	public FlexibleOnTouchListener( long shortPressTriggerTime, ITouchListener listener, boolean interceptEvent )
	{
		this.SHORT_PRESS_TRIGGER_TIME = shortPressTriggerTime;
		this.mTouchListener = listener;
		this.interceptEvent = interceptEvent;
	}



	@Override
	public boolean onTouch( final View view, final MotionEvent event )
	{
		final long timeSpentFromStart = ( event.getEventTime() - eventStartTime );
		switch ( event.getAction() )
		{
			case MotionEvent.ACTION_DOWN:
				initGestureDetector( view );
				initTimer( view, event );
				eventStartTime = event.getEventTime();
				if( mTouchListener != null ) mTouchListener.onDown( view, event );
				viewTouched = true;
				timer.start();
				break;
			case MotionEvent.ACTION_MOVE:
				if( mTouchListener != null ) mTouchListener.onMove( view, event, timeSpentFromStart );
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				viewTouched = false;
				if( timer != null)
					timer.stop();
				if( mTouchListener != null )
					mTouchListener.onUp( view, event, timeSpentFromStart );
				break;
		}
		if( mGestureDetector != null ) mGestureDetector.onTouchEvent( event );
		return interceptEvent;
	}

	private void initGestureDetector( final View view )
	{
		mGestureDetector = new GestureDetector( view.getContext(), new GestureDetector.SimpleOnGestureListener()
		{
			@Override
			public boolean onSingleTapConfirmed( MotionEvent event )
			{
				if( mTouchListener != null ) mTouchListener.onClick( view, event );
				return true;
			}

			@Override
			public void onLongPress( MotionEvent event )
			{
				if( mTouchListener != null ) mTouchListener.onLongPress( view, event );
			}
		} );
	}

	private void initTimer( final View view, final MotionEvent event )
	{
		timer = new CountingTimer( SHORT_PRESS_TRIGGER_TIME, 50, new CountingTimer.ICountingListener()
		{
			@Override
			public void onTimeTickStart() {}

			@Override
			public void onTimeTick( long millis ) {}

			@Override
			public void onTimeTickEnd()
			{
				if( !viewTouched ) return;
				if( mTouchListener != null ) mTouchListener.onShortPress( view, event );
			}

			@Override
			public void onInterrupted(long stopMillis ) {}
		} );
	}

	public interface ITouchListener
	{
		void onLongPress( View view, MotionEvent event );
		/** called from onBackgroundThread */
		void onShortPress( View view, MotionEvent event );
		void onClick( View view, MotionEvent event );
		void onDown( View view, MotionEvent event );
		void onMove( View view, MotionEvent event, long timeSpentFromStart );
		void onUp( View view, MotionEvent event, long timeSpentFromStart );
	}
}