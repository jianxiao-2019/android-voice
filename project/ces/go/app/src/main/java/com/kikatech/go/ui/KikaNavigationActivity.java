package com.kikatech.go.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kikatech.go.R;
import com.kikatech.go.navigation.NavigationManager;
import com.kikatech.go.navigation.google.webservice.GooglePlaceApi;
import com.kikatech.go.navigation.model.PlaceSearchResult;
import com.kikatech.go.navigation.provider.BaseNavigationProvider;
import com.kikatech.go.util.DeviceUtil;
import com.kikatech.go.util.KeyboardUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.util.OverlayUtil;
import com.kikatech.go.util.PermissionUtil;

import java.util.ArrayList;


public class KikaNavigationActivity extends Activity {
    private static final String TAG = "KikaNavigationActivity";

    private View mBtnShowMap;

    private EditText mSearchInput;
    private TextView mSearchResult;
    private View mBtnSearch;

    private EditText mNavigationInput;
    private RadioGroup mNavigationMode;
    private LinearLayout mNavigationAvoid;
    private View mBtnNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        bindView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DeviceUtil.overM() && !OverlayUtil.isPermissionOverlayEnabled(KikaNavigationActivity.this)) {
            OverlayUtil.openSystemSettingsOverlayPage(KikaNavigationActivity.this);
        } else if (!PermissionUtil.hasPermissionLocation(KikaNavigationActivity.this)) {
            PermissionUtil.checkPermissionsLocation(KikaNavigationActivity.this);
        }
    }

    private void bindView() {
        mBtnShowMap = findViewById(R.id.btn_show_map);

        mSearchInput = (EditText) findViewById(R.id.edit_text_search_input);
        mSearchResult = (TextView) findViewById(R.id.search_result);
        mBtnSearch = findViewById(R.id.btn_search);

        mNavigationInput = (EditText) findViewById(R.id.edit_text_navigation_input);
        mNavigationMode = (RadioGroup) findViewById(R.id.navigation_mode);
        mNavigationAvoid = (LinearLayout) findViewById(R.id.navigation_avoid);
        mBtnNavigation = findViewById(R.id.btn_navigation);

        mSearchResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        bindListener();
    }

    private void bindListener() {
        mBtnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.getIns().showMapWithCurrentLocation(KikaNavigationActivity.this);
            }
        });

        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnSearch.setEnabled(false);
                KeyboardUtil.getIns().hideKeyboard(KikaNavigationActivity.this, mSearchInput);

                String locationKeyWord = mSearchInput.getText().toString();

                if (!TextUtils.isEmpty(locationKeyWord)) {
                    GooglePlaceApi.getIns().search(locationKeyWord, new GooglePlaceApi.IOnSearchResultListener() {
                        @Override
                        public void onResult(PlaceSearchResult result) {
                            if (result != null) {
                                if (LogUtil.DEBUG) {
                                    LogUtil.log(TAG, "onResult");
                                    result.print();
                                }
                                mSearchResult.setText(result.getResultText());
                            }
                            mBtnSearch.setEnabled(true);
                        }

                        @Override
                        public void onError(String err) {
                            String errReason = "onError: " + err;

                            if (LogUtil.DEBUG) {
                                LogUtil.logw(TAG, errReason);
                            }
                            mSearchResult.setText(errReason);
                            mBtnSearch.setEnabled(true);
                        }
                    });
                } else {
                    showToast("Please enter search target");
                    mBtnSearch.setEnabled(true);
                }
            }
        });

        mBtnNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String navigationKeyword = mNavigationInput.getText().toString();

                if (!TextUtils.isEmpty(navigationKeyword)) {
                    int modeId = mNavigationMode.getCheckedRadioButtonId();

                    final BaseNavigationProvider.NavigationMode navigationMode;
                    switch (modeId) {
                        case R.id.navigation_mode_walk:
                            navigationMode = BaseNavigationProvider.NavigationMode.WALK;
                            break;
                        case R.id.navigation_mode_bike:
                            navigationMode = BaseNavigationProvider.NavigationMode.BIKE;
                            break;
                        case R.id.navigation_mode_drive:
                        default:
                            navigationMode = BaseNavigationProvider.NavigationMode.DRIVE;
                            break;
                    }

                    ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();

                    for (int i = 0; i < mNavigationAvoid.getChildCount(); i++) {
                        View child = mNavigationAvoid.getChildAt(i);
                        if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                            switch (child.getId()) {
                                case R.id.navigation_avoid_toll:
                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.TOLL);
                                    break;
                                case R.id.navigation_avoid_highway:
                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.HIGHWAY);
                                    break;
                                case R.id.navigation_avoid_ferry:
                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.FERRY);
                                    break;
                            }
                        }
                    }

                    final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);

                    NavigationManager.getIns().startNavigation(KikaNavigationActivity.this, navigationKeyword, navigationMode, avoids);
                } else {
                    showToast("Please enter navigation target");
                }
            }
        });
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
