package com.kikatech.go.accessibility;

import com.kikatech.go.accessibility.scene.Scene;

/**
 * Created by tianli on 17-10-21.
 */

public class AccessibilityManager {

    private static AccessibilityManager sInstance = new AccessibilityManager();

    AccessibilityEventDispatcher mRoot;

    public static AccessibilityManager getInstance(){
        return sInstance;
    }

    private AccessibilityManager(){
    }

    public void registerDispatcher(AccessibilityEventDispatcher dispatcher){
    }

    public void unregisterDispatcher(AccessibilityEventDispatcher dispatcher){
    }

    public void register(Class<Scene> clazz, Object object){
    }

    public void unregister(Class<Scene> clazz, Object object){
    }

    public void onScene(Scene scene){
        if(scene != null){

        }
    }
}

