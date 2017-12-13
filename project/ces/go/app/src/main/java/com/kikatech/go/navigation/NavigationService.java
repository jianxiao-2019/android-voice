package com.kikatech.go.navigation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kikatech.go.R;
import com.kikatech.go.navigation.view.FlexibleOnTouchListener;
import com.kikatech.go.ui.KikaGoActivity;
import com.kikatech.go.ui.ResolutionUtil;
import com.kikatech.go.util.LogUtil;
import com.kikatech.go.view.GoLayout;
import com.kikatech.usb.util.ImageUtil;

/**
 * @author SkeeterWang Created on 2017/10/30.
 */
public class NavigationService extends Service {
    private static final String TAG = "NavigationService";

    private final class Commands {
        private static final String NAVIGATION_SERVICE = "navigation_service_";
        private static final String START_FOREGROUND = NAVIGATION_SERVICE + "start_foreground";
        private static final String STOP_FOREGROUND = NAVIGATION_SERVICE + "stop_foreground";
        private static final String ON_STATUS_CHANGED = NAVIGATION_SERVICE + "on_status_changed";
    }

    private final class Params {
        private static final String STATUS = "status";
    }

    private static final int SERVICE_ID = 200;

    private static WindowManager mWindowManager;
    private static LayoutInflater mLayoutInflater;
    private static LayoutParams mLayoutParam = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_PRIORITY_PHONE,
            LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_HARDWARE_ACCELERATED | LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
    );
    private View mView;
    private ImageView mStatusView;
    private EditText mNavigationInput;
    private RadioGroup mNavigationMode;
    private LinearLayout mNavigationAvoid;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Commands.ON_STATUS_CHANGED:
                    handleStatusChanged(intent);
                    break;
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            switch (intent.getAction()) {
                case Commands.START_FOREGROUND:
                    handleStart(intent);
                    break;
                case Commands.STOP_FOREGROUND:
                    handleStop();
                    break;
            }
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeView();
    }

    private void handleStart(Intent intent) {
        startForeground(SERVICE_ID, getForegroundNotification());
        bindView();

    }

    private void handleStop() {
        if (LogUtil.DEBUG) {
            LogUtil.logw(TAG, "handleStop");
        }
        stopForeground(true);
        removeView();
        unregisterReceiver();
    }

    private void handleStatusChanged(Intent intent) {
        GoLayout.ViewStatus status = (GoLayout.ViewStatus) intent.getSerializableExtra(Params.STATUS);
        Glide.with(NavigationService.this)
                .load(status.getSmallRes())
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mStatusView);
    }

    private void registerReceiver() {
        LocalBroadcastManager.getInstance(NavigationService.this).registerReceiver(mReceiver, new IntentFilter(Commands.ON_STATUS_CHANGED));
    }

    private void unregisterReceiver() {
        try {
            LocalBroadcastManager.getInstance(NavigationService.this).unregisterReceiver(mReceiver);
        } catch (Exception ignore) {
        }
    }

    private void bindView() {
        if (isViewAdded()) {
            return;
        }

        mView = mLayoutInflater.inflate(R.layout.go_layout_gmap, null);

        mStatusView = (ImageView) mView.findViewById(R.id.gmap_status);

        mStatusView.setOnClickListener(new View.OnClickListener() {
            private boolean isActive;
            @Override
            public void onClick(View v) {
                if(!isActive) {
                    isActive = true;
                    registerReceiver();
//                    Glide.with(NavigationService.this)
//                            .load(R.drawable.kika_gmap_awake)
//                            .dontTransform()
//                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                            .into(mStatusView);
                }
            }
        });

        addView();
    }
//    private void bindView() {
//        if (isViewAdded()) {
//            return;
//        }
//        mView = mLayoutInflater.inflate(R.layout.service_navigation, null);
//
//        mNavigationInput = (EditText) mView.findViewById(R.id.edit_text_navigation_input);
//        mNavigationMode = (RadioGroup) mView.findViewById(R.id.navigation_mode);
//        mNavigationAvoid = (LinearLayout) mView.findViewById(R.id.navigation_avoid);
//        View mBtnNavigation = mView.findViewById(R.id.btn_navigation);
//        View mBtnStop = mView.findViewById(R.id.btn_stop_navigation);
//
//        mBtnNavigation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                KeyboardUtil.getIns().hideKeyboard(NavigationService.this, mNavigationInput);
//
//                final String navigationKeyword = mNavigationInput.getText().toString();
//
//                if (!TextUtils.isEmpty(navigationKeyword)) {
//                    int modeId = mNavigationMode.getCheckedRadioButtonId();
//
//                    final BaseNavigationProvider.NavigationMode navigationMode;
//                    switch (modeId) {
//                        case R.id.navigation_mode_walk:
//                            navigationMode = BaseNavigationProvider.NavigationMode.WALK;
//                            break;
//                        case R.id.navigation_mode_bike:
//                            navigationMode = BaseNavigationProvider.NavigationMode.BIKE;
//                            break;
//                        case R.id.navigation_mode_drive:
//                        default:
//                            navigationMode = BaseNavigationProvider.NavigationMode.DRIVE;
//                            break;
//                    }
//
//                    ArrayList<BaseNavigationProvider.NavigationAvoid> avoidList = new ArrayList<>();
//
//                    for (int i = 0; i < mNavigationAvoid.getChildCount(); i++) {
//                        View child = mNavigationAvoid.getChildAt(i);
//                        if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
//                            switch (child.getId()) {
//                                case R.id.navigation_avoid_toll:
//                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.TOLL);
//                                    break;
//                                case R.id.navigation_avoid_highway:
//                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.HIGHWAY);
//                                    break;
//                                case R.id.navigation_avoid_ferry:
//                                    avoidList.add(BaseNavigationProvider.NavigationAvoid.FERRY);
//                                    break;
//                            }
//                        }
//                    }
//
//                    final BaseNavigationProvider.NavigationAvoid[] avoids = avoidList.toArray(new BaseNavigationProvider.NavigationAvoid[0]);
//
//                    NavigationManager.getIns().startNavigation(NavigationService.this, navigationKeyword, navigationMode, avoids);
//                } else {
//                    showToast("Please enter navigation target");
//                }
//            }
//        });
//
//        mBtnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NavigationManager.getIns().stopNavigation(NavigationService.this);
//                backToKikaGoActivity();
//                handleStop();
//            }
//        });
//
//        mView.setOnTouchListener(mOnTouchListener);
//
//        addView();
//    }

    private void backToKikaGoActivity() {
        try {
            Intent intent = new Intent(NavigationService.this, KikaGoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(NavigationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (Exception ignore) {
        }
    }

    private void addView() {
        try {
            // mLayoutParam.softInputMode = LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_ADJUST_PAN;
            mLayoutParam.gravity = Gravity.TOP | Gravity.RIGHT;
            mLayoutParam.x = ResolutionUtil.dp2px(NavigationService.this, 14);
            mLayoutParam.y = ResolutionUtil.dp2px(NavigationService.this, 14);
            mWindowManager.addView(mView, mLayoutParam);
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }
    }

    private void removeView() {
        try {
            mWindowManager.removeView(mView);
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }
    }

    private boolean isViewAdded() {
        try {
            return mView != null && mView.getWindowToken() != null;
        } catch (Exception ignore) {
        }
        return true;
    }

    private FlexibleOnTouchListener mOnTouchListener = new FlexibleOnTouchListener(100, new FlexibleOnTouchListener.ITouchListener() {

        private int[] viewOriginalXY = new int[2];
        private float[] eventOriginalXY = new float[2];
        private int[] deltaXY = new int[2];

        @Override
        public void onLongPress(View view, MotionEvent event) {
        }

        @Override
        public void onShortPress(View view, MotionEvent event) {
        }

        @Override
        public void onClick(View view, MotionEvent event) {
        }

        @Override
        public void onDown(View view, MotionEvent event) {
            viewOriginalXY = new int[]{mLayoutParam.x, mLayoutParam.y};
            eventOriginalXY = new float[]{event.getRawX(), event.getRawY()};
        }

        @Override
        public void onMove(View view, MotionEvent event, long timeSpentFromStart) {
            deltaXY = new int[]{(int) (event.getRawX() - eventOriginalXY[0]), (int) (event.getRawY() - eventOriginalXY[1])};
            mLayoutParam.x = getValidX(viewOriginalXY[0], deltaXY[0]);
            mLayoutParam.y = getValidY(viewOriginalXY[1], deltaXY[1]);
            mWindowManager.updateViewLayout(mView, mLayoutParam);
        }

        @Override
        public void onUp(View view, MotionEvent event, long timeSpentFromStart) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "x: " + mLayoutParam.x + ", y: " + mLayoutParam.y);
        }

        private int getValidX(int viewOriginalX, int deltaX) {
            if (LogUtil.DEBUG) LogUtil.log(TAG, "deltaX: " + deltaX);
            int boundLeft = 0;
            int boundRight = getDeviceWidthByOrientation() - mView.getMeasuredWidth();
            return (deltaX > 0)
                    ? (viewOriginalX + deltaX < boundRight) ? viewOriginalX + deltaX : boundRight
                    : (viewOriginalX + deltaX >= boundLeft) ? viewOriginalX + deltaX : boundLeft;
        }

        private int getValidY(int viewOriginalY, int deltaY) {
            int boundTop = 0;
            int boundBottom = getDeviceHeightByOrientation() - mView.getMeasuredHeight();
            return (deltaY > 0)
                    ? (viewOriginalY + deltaY < boundBottom) ? viewOriginalY + deltaY : boundBottom
                    : (viewOriginalY + deltaY >= boundTop) ? viewOriginalY + deltaY : boundTop;
        }

        private int getDeviceWidthByOrientation() {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            switch (getCurrentOrientation()) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    //noinspection SuspiciousNameCombination
                    return displayMetrics.heightPixels;
                default:
                case Configuration.ORIENTATION_PORTRAIT:
                    return displayMetrics.widthPixels;
            }
        }

        private int getDeviceHeightByOrientation() {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            switch (getCurrentOrientation()) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    //noinspection SuspiciousNameCombination
                    return displayMetrics.widthPixels;
                default:
                case Configuration.ORIENTATION_PORTRAIT:
                    return displayMetrics.heightPixels;
            }
        }

        private DisplayMetrics getDisplayMetrics() {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics;
        }

        private int getCurrentOrientation() {
            Configuration configuration = getResources().getConfiguration();
            return configuration.orientation;
        }
    });


    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public static void processStart(Context context) {
        Bundle args = new Bundle();
        launchCommend(context, Commands.START_FOREGROUND, args);
    }

    public static void processStop(Context context) {
        Bundle args = new Bundle();
        launchCommend(context, Commands.STOP_FOREGROUND, args);
    }

    public static void processStatusChanged(Context context, GoLayout.ViewStatus status) {
        Intent intent = new Intent(Commands.ON_STATUS_CHANGED);
        intent.putExtra(Params.STATUS, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    private static void launchCommend(Context context, String action, Bundle args) {
        try {
            Context appCtx = context.getApplicationContext();
            Intent notifyIntent = new Intent(appCtx, NavigationService.class);
            notifyIntent.setAction(action);
            notifyIntent.putExtras(args);
            appCtx.startService(notifyIntent);
        } catch (Exception e) {
            if (LogUtil.DEBUG) LogUtil.printStackTrace(TAG, e.getMessage(), e);
        }
    }


    private Notification getForegroundNotification() {
        Intent closeIntent = new Intent(NavigationService.this, NavigationService.class);
        closeIntent.setAction(Commands.STOP_FOREGROUND);
        PendingIntent closePendingIntent = PendingIntent.getService(NavigationService.this, SERVICE_ID, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(NavigationService.this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(ImageUtil.safeDecodeFile(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(TAG)
                .setContentText("Click to close service.")
                .setContentIntent(closePendingIntent)
                .setAutoCancel(true)
                // .setColor( appCtx.getResources().getColor( R.color.gela_green ) )
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
