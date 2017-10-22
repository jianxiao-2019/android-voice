package com.kikatech.go.accessibility;

import com.kikatech.go.accessibility.scene.Scene;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tianli on 17-10-21.
 */

public class AccessibilityManager {

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

    public void register(Class<Scene> clazz, Object object){
        if(clazz != null && object != null){
            if(mSubscribers.get(clazz.getName()) == null){
                mSubscribers.put(clazz.getName(), Arrays.asList(object));
            }else{
                if(!mSubscribers.get(clazz.getName()).contains(object)){
                    mSubscribers.get(clazz.getName()).add(object);
                }
            }
        }
    }

    public void unregister(Class<Scene> clazz, Object object){
        if(clazz != null){
            if(mSubscribers.get(clazz.getName()) != null){
                mSubscribers.get(clazz.getName()).remove(object);
            }
        }
    }

    public void onScene(Scene scene){
        if(scene != null){
            String name = scene.getClass().getName();
            if(mSubscribers.get(name) != null){
                // notify all
            }
        }
    }
}

