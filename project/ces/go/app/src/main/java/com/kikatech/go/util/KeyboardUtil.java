package com.kikatech.go.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * @author wangskeeter Created on 16/8/19.
 */
public class KeyboardUtil
{
	private static final String TAG = "KeyboardUtils";

	private static KeyboardUtil sIns;

	public static synchronized KeyboardUtil getIns()
	{
		if( sIns == null )
			sIns = new KeyboardUtil();
		return sIns;
	}

	private KeyboardUtil() {}

	public void showKeyboard( Context context, EditText editText )
	{
		InputMethodManager inputMethodManager = ( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE );
		editText.requestFocus();
		inputMethodManager.showSoftInput( editText, InputMethodManager.SHOW_IMPLICIT );
	}

	public void hideKeyboard( Context context, EditText editText )
	{
		InputMethodManager inputMethodManager = ( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE );
		inputMethodManager.hideSoftInputFromWindow( editText.getWindowToken(), 0 );
		editText.clearFocus();
	}
}
