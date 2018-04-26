package com.kikatech.go.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kikatech.go.R;

import java.util.ArrayList;

/**
 * @author SkeeterWang Created on 2018/4/13.
 */

public class FAQ1DialogAdapter extends PagerAdapter {

    private ArrayList<View> mList = new ArrayList<>();

    public FAQ1DialogAdapter(Context context, int[] resources, int size) {
        for (int i = 0; i < size; i++) {
            View pagerItem = LayoutInflater.from(context).inflate(R.layout.dialog_faq_pager_item_1, null);
            ((ImageView) pagerItem.findViewById(R.id.dialog_faq_pager_item_img)).setImageResource(resources[i]);
            mList.add(pagerItem);
        }
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
        View mItemView = mList.get(position);
        container.addView(mItemView);
        return mItemView;
    }
}