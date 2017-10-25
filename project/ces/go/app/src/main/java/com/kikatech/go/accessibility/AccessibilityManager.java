package com.kikatech.go.accessibility;

import com.kikatech.go.accessibility.scene.Scene;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tianli on 17-10-21.
 */

public class AccessibilityManager {

    private static final String TAG = AccessibilityManager.class.getSimpleName();

    private static AccessibilityManager sInstance = new AccessibilityManager();

    private HashMap<String, List<Object>> mSubscribers = new HashMap<>();

    AccessibilityEventDispatcher mRoot;

    public static AccessibilityManager getInstance(){
        return sInstance;
    }

    private AccessibilityManager(){
    }

    public void registerDispatcher(AccessibilityEventDispatcher dispatcher){
        if(mRoot == null){
            mRoot = dispatcher;
        }else{
            AccessibilityEventDispatcher iterator = mRoot;
            while (iterator != null){
                if(dispatcher == iterator){
                    return;
                }
                if(iterator.mChain == null){
                    iterator.mChain = dispatcher;
                    dispatcher.mChain = null;
                    return;
                }
                iterator = iterator.mChain;
            }
        }
    }

    public void unregisterDispatcher(AccessibilityEventDispatcher dispatcher){
        AccessibilityEventDispatcher iterator = mRoot;
        AccessibilityEventDispatcher parent = null;
        while (iterator != null){
            if(dispatcher == iterator){
                if(parent != null){
                    parent.mChain = iterator.mChain;
                }else{
                    mRoot = null;
                }
                return;
            }
            parent = iterator;
            iterator = iterator.mChain;
        }
    }

    public void register(Class<?> clazz, Object object){
        if(clazz != null && object != null){
            if(mSubscribers.get(clazz.getName()) == null){
                List<Object> objectList = new ArrayList<>();
                objectList.add(object);
                mSubscribers.put(clazz.getName(), objectList);
            }else{
                if(!mSubscribers.get(clazz.getName()).contains(object)){
                    mSubscribers.get(clazz.getName()).add(object);
                }
            }
        }
    }

    public void unregister(Class<?> clazz, Object object){
        if(clazz != null){
            if(mSubscribers.get(clazz.getName()) != null){
                mSubscribers.get(clazz.getName()).remove(object);
            }
        }
    }

    public void onScene(Scene scene) {
        if(scene != null) {
            String name = scene.getClass().getName();
            List<Object> subscribers = mSubscribers.get(name);
            for(Object subscriber : subscribers) {
                // notify all
                Method[] methods = subscriber.getClass().getMethods();
                for(Method method : methods) {
                    if("onSceneShown".equals(method.getName())) {
                        try {
                            method.invoke(subscriber, scene);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

