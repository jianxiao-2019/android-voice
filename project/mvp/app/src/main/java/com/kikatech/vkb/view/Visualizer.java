package com.kikatech.vkb.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kikatech.vkb.R;
import com.kikatech.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Visualizer extends LinearLayout implements Runnable {
    private static final long ANIMATION_DURATION = 50;

    private int VISUALIZER_NUM_WAVES = 15;
    private int VISUALIZER_GRAVITY = 0;

    private int LINE_WIDTH = 16;
    private int LINE_MIN_WIDTH = 16;
    private int LINE_MIN_HEIGHT = 20;
    private int LINE_SPACING = 10;
    private int LINE_BORDER_RADIUS = 50;
    private static final int MAX_NUM_NO_RANDOM_UPDATE = 3;

    private int COLOR_UNIFORM = android.R.color.black;

    private Context context;
    private final Handler uiHandler;
    private LayoutParams params;
    private ArrayList<View> waveList = new ArrayList<>();
    private ValueAnimator valueAnimator;
    private boolean mIsRecording;
    private boolean mHideView;
    private List<LineData> mDatas;
    private Random mRandom = new Random();
    private int mViewHeight;
    private int mNumNoUpdate = 0;

    public Visualizer(Context context) {
        super(context);
        this.context = context;
        if (!isInEditMode()) {
            this.init();
        }

        uiHandler = new Handler();
    }

    public Visualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        if (!isInEditMode()) {
            this.attributes(attrs);
            this.init();
        }

        uiHandler = new Handler();
    }

    public Visualizer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        if (!isInEditMode()) {
            this.attributes(attrs);
            this.init();
        }

        uiHandler = new Handler();
    }

    private void attributes(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.audiowaves__style,
                0, 0);

        try {
            VISUALIZER_NUM_WAVES = a.getInteger(R.styleable.audiowaves__style_aw_num_waves, VISUALIZER_NUM_WAVES);
            VISUALIZER_GRAVITY = a.getInteger(R.styleable.audiowaves__style_aw_gravity, VISUALIZER_GRAVITY);

            LINE_WIDTH = DensityUtil.dp2px(context, a.getInteger(R.styleable.audiowaves__style_aw_line_width, LINE_WIDTH));
            LINE_MIN_WIDTH = DensityUtil.dp2px(context, a.getInteger(R.styleable.audiowaves__style_aw_line_min_width, LINE_MIN_WIDTH));
            LINE_MIN_HEIGHT = DensityUtil.dp2px(context, a.getInteger(R.styleable.audiowaves__style_aw_line_min_height, LINE_MIN_HEIGHT));
            LINE_SPACING = DensityUtil.dp2px(context, a.getInteger(R.styleable.audiowaves__style_aw_line_spacing, LINE_SPACING));
            LINE_BORDER_RADIUS = DensityUtil.dp2px(context, a.getInteger(R.styleable.audiowaves__style_aw_line_border_radius, LINE_BORDER_RADIUS));

            COLOR_UNIFORM = a.getColor(R.styleable.audiowaves__style_aw_color_uniform, getResources().getColor(COLOR_UNIFORM));
        } finally {
            a.recycle();
        }
    }

    private void init() {
        setLayoutParams(
                new LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        setOrientation(HORIZONTAL);

        switch (VISUALIZER_GRAVITY) {
            case 0:
                setGravity(Gravity.CENTER);
                break;
            case 1:
                setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                break;
            case 2:
                setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                break;
            case 3:
                setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                break;
            case 4:
                setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                break;
        }

        addWaves();
        prepare();
    }

    private void addWaves() {
        params = new LayoutParams(LINE_MIN_WIDTH, LINE_MIN_HEIGHT);
        params.setMargins(LINE_SPACING, 0, LINE_SPACING, 0);

        for (int i = 0; i < VISUALIZER_NUM_WAVES; i++) {
            View v = new View(context);
            v.setLayoutParams(params);
            setBackground(v);
            waveList.add(v);
            addView(v);
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(ANIMATION_DURATION - 10);
    }

    private void setBackground(View v) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(LINE_BORDER_RADIUS);
        gd.setGradientRadius(90.f);
        gd.setColors(new int[]{COLOR_UNIFORM, COLOR_UNIFORM});
        v.setBackground(gd);
    }

    private void prepare() {
        mDatas = new ArrayList();
        for (int j = 0; j < VISUALIZER_NUM_WAVES; j++) {
            LineData localLineData = new LineData();
            localLineData.height = LINE_MIN_HEIGHT;
            mDatas.add(localLineData);
        }
    }

    private void updateToOriginStatus() {
        for (int i = 0; i < waveList.size(); i++) {
            params = new LayoutParams(LINE_WIDTH, LINE_MIN_HEIGHT);
            params.setMargins(LINE_SPACING, 0, LINE_SPACING, 0);
            waveList.get(i).setLayoutParams(params);
        }
    }

    public void startListening() {
        mIsRecording = true;
        mHideView = false;
        setVisibility(VISIBLE);
        uiHandler.postDelayed(this, 100);
    }

    public void stopListening(boolean hideView) {
        mIsRecording = false;
        mHideView = hideView;
    }

    public void setVolume(int volume) {
//        Log.i("kikavoice", "[Visualizer] setVolume: " + volume);
        mViewHeight = getHeight();
        int maxVolume = volume * mViewHeight / 1000;
        maxVolume = maxVolume >= LINE_MIN_HEIGHT ? maxVolume : LINE_MIN_HEIGHT;

        // 如果音量太小，动画不明显，做一下放大
        maxVolume = mViewHeight / 5 + maxVolume * 4 / 5;

        // 使用拉格朗日算法计算
        int lagrangeNum = 3; // Lagrange差值数组个数

        double x[] = new double[2];
        double y[] = new double[2];
        double x0[] = new double[lagrangeNum];

        // 设置Lagrange差值已知坐标
        x[0] = 0;
        y[0] = LINE_MIN_HEIGHT;
        x[1] = 4 * (LINE_WIDTH + LINE_SPACING);
        y[1] = maxVolume;

        // 设置Lagrange差值需求x坐标
        for (int i = 0; i < lagrangeNum; i++) {
            x0[i] = (LINE_WIDTH + LINE_SPACING) * (i + 1);
        }
        double y0[] = LagMethod(x, y, x0);


        mDatas.get(0).height = LINE_MIN_HEIGHT;

        // 动画分为3段，第1段和第3段任意随机显示，第2段随机显示波峰，按波形显示
        int i0 = mRandom.nextInt(3);
        int i1 = mRandom.nextInt(3);
        int i2 = mRandom.nextInt(3);
        int i3 = mRandom.nextInt(3);
        mDatas.get(1).height = calcLineHeightOrMin((int) y0[i0]);
        mDatas.get(2).height = calcLineHeightOrMin((int) y0[i1]);
        mDatas.get(3).height = calcLineHeightOrMin((int) y0[i2]);
        mDatas.get(4).height = calcLineHeightOrMin((int) y0[i3]);

        int m = mRandom.nextInt(5);
        int[] origWaveList = {(int) y0[0], (int) y0[2], maxVolume, (int) y0[2], (int) y0[0]};
        int[] destWaveList = new int[5];
        destWaveList[0] = origWaveList[m];
        destWaveList[1] = origWaveList[m + 1 > 4 ? m + 1 - 4 : m + 1];
        destWaveList[2] = origWaveList[m + 2 > 4 ? m + 2 - 4 : m + 2];
        destWaveList[3] = origWaveList[m + 3 > 4 ? m + 3 - 4 : m + 3];
        destWaveList[4] = origWaveList[m + 4 > 4 ? m + 4 - 4 : m + 4];
        mDatas.get(5).height = calcLineHeightOrMin(destWaveList[0]);
        mDatas.get(6).height = calcLineHeightOrMin(destWaveList[1]);
        mDatas.get(7).height = calcLineHeightOrMin(destWaveList[2]);
        mDatas.get(8).height = calcLineHeightOrMin(destWaveList[3]);
        mDatas.get(9).height = calcLineHeightOrMin(destWaveList[4]);

        int j0 = mRandom.nextInt(3);
        int j1 = mRandom.nextInt(3);
        int j2 = mRandom.nextInt(3);
        int j3 = mRandom.nextInt(3);
        mDatas.get(10).height = calcLineHeightOrMin((int) y0[j0]);
        mDatas.get(11).height = calcLineHeightOrMin((int) y0[j1]);
        mDatas.get(12).height = calcLineHeightOrMin((int) y0[j2]);
        mDatas.get(13).height = calcLineHeightOrMin((int) y0[j3]);
        mDatas.get(14).height = LINE_MIN_HEIGHT;
    }

    private int calcLineHeightOrMin(int height) {
        return height > LINE_MIN_HEIGHT ? height : LINE_MIN_HEIGHT;
    }

    /**
     * 拉格朗日插值法 (Lagrange Interpolating Polynomial)
     *
     * @param X  已知插值点x坐标
     * @param Y  已知插值点y坐标
     * @param X0 需求插值点x坐标
     * @return 需求插值点y坐标
     */
    private static double[] LagMethod(double X[], double Y[], double X0[]) {
        int m = X.length;
        int n = X0.length;
        double Y0[] = new double[n];
        for (int i1 = 0; i1 < n; i1++) { // 遍历X0
            double t = 0;
            for (int i2 = 0; i2 < m; i2++) { // 遍历Y
                double u = 1;
                for (int i3 = 0; i3 < m; i3++) { // 遍历X
                    if (i2 != i3) {
                        u = u * (X0[i1] - X[i3]) / (X[i2] - X[i3]);
                    }
                }
                u = u * Y[i2];
                t = t + u;
            }
            Y0[i1] = t;
        }
        return Y0;
    }

    @Override
    public void run() {
        uiHandler.removeCallbacks(this);
        valueAnimator.cancel();

        // Check if stop recording
        if (!mIsRecording) {
            //Log.i("kikavoice", "[Visualizer] updateToOriginStatus");
            updateToOriginStatus();
            if (mHideView) {
                setVisibility(GONE);
            }
            return;
        }

        // Check if need to update mDatas
        boolean isRealUpdate = false;
        for (int i = 0; i < mDatas.size(); i++) {
            if (mDatas.get(i).height != mDatas.get(i).lastHeight) {
                isRealUpdate = true;
                break;
            }
        }
        if (isRealUpdate) { // Update lastHeight
            mNumNoUpdate = 0;
            //Log.i("kikavoice", "[Visualizer] Update lastHeight, mNumNoUpdate: " + mNumNoUpdate);
            for (int i = 0; i < mDatas.size(); i++) {
                mDatas.get(i).lastHeight = mDatas.get(i).height;
            }
        } else if (!isRealUpdate
                && ++mNumNoUpdate > MAX_NUM_NO_RANDOM_UPDATE) { // 随机变化一下mDatas的值, 取值范围是(LINE_MIN_HEIGHT, mViewHeight/3), 这种情况不用附加属性动画
            //Log.i("kikavoice", "[Visualizer] random to display, mNumNoUpdate: " + mNumNoUpdate);
            mDatas.get(0).height = LINE_MIN_HEIGHT;
            mDatas.get(1).height = LINE_MIN_HEIGHT;
            mDatas.get(2).height = LINE_MIN_HEIGHT;
            for (int i = 3; i < mDatas.size() - 3; i++) {
                Random random = new Random();
                int n = Math.abs(mViewHeight / 5 - LINE_MIN_HEIGHT);
                n = n > 0 ? n : 1;
                mDatas.get(i).height = LINE_MIN_HEIGHT + random.nextInt(n);
            }
            mDatas.get(mDatas.size() - 3).height = LINE_MIN_HEIGHT;
            mDatas.get(mDatas.size() - 2).height = LINE_MIN_HEIGHT;
            mDatas.get(mDatas.size() - 1).height = LINE_MIN_HEIGHT;

            for (int i = 0; i < waveList.size(); i++) {
                int waveHeight = mDatas.get(i).height;
                params = new LayoutParams(
                        LINE_WIDTH,
                        waveHeight < LINE_MIN_HEIGHT ? LINE_MIN_HEIGHT : waveHeight);
                params.setMargins(LINE_SPACING, 0, LINE_SPACING, 0);
                waveList.get(i).setLayoutParams(params);
            }

            uiHandler.postDelayed(this, ANIMATION_DURATION);
            return;
        } else {
            //Log.i("kikavoice", "[Visualizer] other case, isRealUpdate: " + isRealUpdate + ", mNumNoUpdate: " + mNumNoUpdate);
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                for (int i = 0; i < waveList.size(); i++) {
                    int smallValue = mDatas.get(i).height > mDatas.get(i).lastHeight ? mDatas.get(i).lastHeight : mDatas.get(i).height;
                    int waveHeight = (int) (smallValue + Math.abs(mDatas.get(i).height - mDatas.get(i).lastHeight) * val);
                    params = new LayoutParams(
                            LINE_WIDTH,
                            waveHeight < LINE_MIN_HEIGHT ? LINE_MIN_HEIGHT : waveHeight);
                    params.setMargins(LINE_SPACING, 0, LINE_SPACING, 0);
                    waveList.get(i).setLayoutParams(params);
                }
            }
        });
        valueAnimator.start();

        uiHandler.postDelayed(this, ANIMATION_DURATION);
    }

    private class LineData {
        public int height;
        public int lastHeight; // 用于保存上一次的值，如果超过 MAX_NUM_NO_RANDOM_UPDATE 次，随机更新 （为了避免音量没有变化，给用户卡顿的感觉）
    }

}
