package com.kikatech.go.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.notification.NotificationListenerUtil;
import com.kikatech.go.telephony.TelephonyServiceManager;
import com.kikatech.go.util.PermissionUtil;


/**
 * @author SkeeterWang Created on 2017/10/27.
 */
public class TelephonyActivity extends AppCompatActivity
{
	private static final String TAG = "TelephonyActivity";

	public static final String ACTION_CONTROL_PHONE_CALL = "action_control_phone_call";

	private View mMakePhoneCallView;
	private EditText mNumberInput;
	private View mBtnMakePhoneCall;

	private View mControlPhoneCallView;
	private View mBtnAnswerPhoneCall;
	private View mBtnKillPhoneCall;

	private TextView mBtnTurnOnOffSpeaker;

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_telephony );
		bindView();
	}

	@Override
	protected void onNewIntent( Intent intent )
	{
		super.onNewIntent( intent );
		setIntent( intent );
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		handleOnResume();

		if( !NotificationListenerUtil.isPermissionNLEnabled( TelephonyActivity.this ) ) {
			NotificationListenerUtil.openSystemSettingsNLPage( TelephonyActivity.this );
		}
		else if( !PermissionUtil.hasPermissions( TelephonyActivity.this, PermissionUtil.Permission.CALL_PHONE,
												 PermissionUtil.Permission.READ_PHONE_STATE ) ) {
			PermissionUtil.checkPermission( TelephonyActivity.this, PermissionUtil.Permission.CALL_PHONE, PermissionUtil.Permission.READ_PHONE_STATE );
		}
	}

	private void bindView()
	{
		mMakePhoneCallView = findViewById( R.id.make_phone_call_ll );
		mNumberInput = ( EditText ) findViewById( R.id.edit_text_number_input );
		mBtnMakePhoneCall = findViewById( R.id.btn_make_phone_call );

		mControlPhoneCallView = findViewById( R.id.control_phone_call_ll );
		mBtnAnswerPhoneCall = findViewById( R.id.btn_answer_phone_call );
		mBtnKillPhoneCall = findViewById( R.id.btn_kill_phone_call );

		mBtnTurnOnOffSpeaker = ( TextView ) findViewById( R.id.btn_turn_on_off_speaker );
		adjustBtnSpeakerLayout( TelephonyServiceManager.getIns().isSpeakerOn( TelephonyActivity.this ) );

		bindListener();
	}

	private void bindListener()
	{
		mBtnMakePhoneCall.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {

				String phoneNumber = mNumberInput.getText().toString();

				if( !TextUtils.isEmpty( phoneNumber ) ) {
					if( !PermissionUtil.hasPermissionPhone( TelephonyActivity.this ) ) {
						PermissionUtil.checkPermissionsPhone( TelephonyActivity.this );
					}
					else {
						TelephonyServiceManager.getIns().makePhoneCall( TelephonyActivity.this, phoneNumber );
					}
				}
				else {
					Toast.makeText( TelephonyActivity.this, "Please enter phone number", Toast.LENGTH_SHORT ).show();
				}
			}
		} );

		mBtnAnswerPhoneCall.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				if( !PermissionUtil.hasPermissionPhone( TelephonyActivity.this ) ) {
					PermissionUtil.checkPermissionsPhone( TelephonyActivity.this );
				}
				else {
					TelephonyServiceManager.getIns().answerPhoneCall( TelephonyActivity.this );
				}
			}
		} );

		mBtnKillPhoneCall.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				if( !PermissionUtil.hasPermissionPhone( TelephonyActivity.this ) ) {
					PermissionUtil.checkPermissionsPhone( TelephonyActivity.this );
				}
				else {
					TelephonyServiceManager.getIns().killPhoneCall( TelephonyActivity.this );
				}
			}
		} );

		mBtnTurnOnOffSpeaker.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				Context context = TelephonyActivity.this;
				boolean isSpeakerOn = TelephonyServiceManager.getIns().isSpeakerOn( context );
				if( isSpeakerOn ) {
					TelephonyServiceManager.getIns().turnOffSpeaker( context );
					adjustBtnSpeakerLayout( false );
				}
				else {
					TelephonyServiceManager.getIns().turnOnSpeaker( context );
					adjustBtnSpeakerLayout( true );
				}
			}
		} );
	}

	private void adjustBtnSpeakerLayout( boolean isSpeakerOn )
	{
		mBtnTurnOnOffSpeaker.setText( isSpeakerOn ? "Turn off speaker" : "Turn on speaker" );
	}

	private void handleOnResume()
	{
		Intent intent = getIntent();

		boolean isControlMode = false;

		if( intent != null )
		{
			String action = intent.getAction();

			if( !TextUtils.isEmpty( action ) )
			{
				switch( action )
				{
					case ACTION_CONTROL_PHONE_CALL:
						isControlMode = true;
						break;
				}
			}
		}

		adjustLayout( isControlMode );

		setIntent( null );
	}

	private void adjustLayout( boolean isControlMode )
	{
		if( isControlMode )
		{
			mMakePhoneCallView.setVisibility( View.GONE );
			mControlPhoneCallView.setVisibility( View.VISIBLE );
		}
		else
		{
			mMakePhoneCallView.setVisibility( View.VISIBLE );
			mControlPhoneCallView.setVisibility( View.GONE );
		}
	}
}
