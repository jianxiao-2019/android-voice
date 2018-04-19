package com.kikatech.go.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kikatech.go.R;

import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2018/4/13.
 */

public class FAQ1DialogAdapter extends PagerAdapter {

    private ArrayList<View> mList = new ArrayList<>();

    public FAQ1DialogAdapter(Context context) {
        View pagerItem1 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
        View pagerItem2 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
        View pagerItem3 = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);

        Drawable drawable1 = context.getResources().getDrawable(R.drawable.bg_dialog_tmp);
        drawable1.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
        pagerItem1.setBackground(drawable1);

        Drawable drawable2 = context.getResources().getDrawable(R.drawable.bg_dialog_tmp);
        drawable2.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        pagerItem2.setBackground(drawable2);

        Drawable drawable3 = context.getResources().getDrawable(R.drawable.bg_dialog_tmp);
        drawable3.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        pagerItem3.setBackground(drawable3);

        mList.add(pagerItem1);
        mList.add(pagerItem2);
        mList.add(pagerItem3);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mList.get(position));
        return mList.get(position);
    }
}