package com.kikatech.go.accessibility;

import com.google.common.collect.EvictingQueue;
import com.kikatech.go.accessibility.scene.Scene;
import com.kikatech.go.util.StringUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tianli on 17-10-21.
 */

public class AccessibilityManager {

    private static final String TAG = "AccessibilityManager";

    private static AccessibilityManager sInstance = new AccessibilityManager();

    private HashMap<String, List<Object>> mSubscribers = new HashMap<>();

    AccessibilityEventDispatcher mRoot;

    private static EvictingQueue<ActivityInfo> mRecentActivity = EvictingQueue.create(30);

    public static AccessibilityManager getInstance() {
        return sInstance;
    }

    private AccessibilityManager() {
    }

    public void registerDispatcher(AccessibilityEventDispatcher dispatcher) {
        if (mRoot == null) {
            mRoot = dispatcher;
        } else {
            AccessibilityEventDispatcher iterator = mRoot;
            while (iterator != null) {
                if (dispatcher == iterator) {
                    return;
                }
                if (iterator.mChain == null) {
                    iterator.mChain = dispatcher;
                    dispatcher.mChain = null;
                    return;
                }
                iterator = iterator.mChain;
            }
        }
    }

    public void unregisterDispatcher(AccessibilityEventDispatcher dispatcher) {
        AccessibilityEventDispatcher iterator = mRoot;
        AccessibilityEventDispatcher parent = null;
        while (iterator != null) {
            if (dispatcher == iterator) {
                if (parent != null) {
                    parent.mChain = iterator.mChain;
                } else {
                    mRoot = null;
                }
                return;
            }
            parent = iterator;
            iterator = iterator.mChain;
        }
    }

    public synchronized void register(Class<?> clazz, Object object) {
        if (clazz != null && object != null) {
            if (mSubscribers.get(clazz.getName()) == null) {
                List<Object> objectList = new ArrayList<>();
                objectList.add(object);
                mSubscribers.put(clazz.getName(), objectList);
            } else {
                if (!mSubscribers.get(clazz.getName()).contains(object)) {
                    mSubscribers.get(clazz.getName()).add(object);
                }
            }
        }
    }

    public synchronized void unregister(Class<?> clazz, Object object) {
        if (clazz != null) {
            List<Object> list = mSubscribers.get(clazz.getName());
            if (list != null) {
                list.remove(object);
                if (list.isEmpty()) {
                    mSubscribers.remove(clazz.getName());
                }
            }
        }
    }

    public synchronized void onScene(Scene scene) {
        if (scene != null) {
            String name = scene.getClass().getName();
            List<Object> subscribers = mSubscribers.get(name);
            if(subscribers == null) {
                return;
            }
            for (Object subscriber : subscribers) {
                // notify all
                Method[] methods = subscriber.getClass().getMethods();
                for (Method method : methods) {
                    if ("onSceneShown".equals(method.getName())) {
                        try {
                            method.invoke(subscriber, scene);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
    }

    public void recordActivity(String packageName, String className) {
        ActivityInfo activityInfo = new ActivityInfo(packageName, className);
        mRecentActivity.add(activityInfo);
    }

    public boolean isAppRecentUsed(String packageName) {
        for (ActivityInfo activityInfo : mRecentActivity) {
            if (StringUtil.equals(activityInfo.getPackageName(), packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAppOnTop(String packageName) {
        if (mRecentActivity.size() > 0) {
            final Iterator<ActivityInfo> itr = mRecentActivity.iterator();
            ActivityInfo lastActivityInfo = itr.next();
            while (itr.hasNext()) {
                lastActivityInfo = itr.next();
            }
            return StringUtil.equals(lastActivityInfo.getPackageName(), packageName);
        }
        return false;
    }

    public String getTopApp() {
        if (mRecentActivity.size() > 0) {
            final Iterator<ActivityInfo> itr = mRecentActivity.iterator();
            ActivityInfo lastActivityInfo = itr.next();
            while (itr.hasNext()) {
                lastActivityInfo = itr.next();
            }
            return lastActivityInfo.getActivityName();
        }
        return null;
    }

    private static class ActivityInfo {

        private String mPackageName;
        private String mActivityName;

        public ActivityInfo(String packageName, String activityName) {
            mPackageName = packageName;
            mActivityName = activityName;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getActivityName() {
            return mActivityName;
        }
    }
}

