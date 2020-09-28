package com.sunland.cpocr.crash;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

public class ApplicationHelper {
    public static final boolean PRE_ICS = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    /*
     * Register.
     */

    /**
     * Registers a callback to be called following the life cycle of the
     * application's {@link android.app.Activity activities}.
     *
     * @param application The application with which to register the callback.
     * @param callback    The callback to register.
     */
    public static void registerActivityLifecycleCallbacks(
            Application application, ActivityLifecycleCallbacksCompat callback) {
        if (PRE_ICS) {
            preIcsRegisterActivityLifecycleCallbacks(callback);
        } else {
            postIcsRegisterActivityLifecycleCallbacks(application, callback);
        }
    }

    private static void preIcsRegisterActivityLifecycleCallbacks(
            ActivityLifecycleCallbacksCompat callback) {
        MainLifecycleDispatcher.get().registerActivityLifecycleCallbacks(
                callback);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void postIcsRegisterActivityLifecycleCallbacks(
            Application application, ActivityLifecycleCallbacksCompat callback) {
        application
                .registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(
                        callback));
    }

    /*
     * Unregister.
     */

    /**
     * Unregisters a previously registered callback.
     *
     * @param application The application with which to unregister the callback.
     * @param callback    The callback to unregister.
     */
    public static void unregisterActivityLifecycleCallbacks(
            Application application, ActivityLifecycleCallbacksCompat callback) {
        if (PRE_ICS) {
            preIcsUnregisterActivityLifecycleCallbacks(callback);
        } else {
            postIcsUnregisterActivityLifecycleCallbacks(application, callback);
        }
    }

    private static void preIcsUnregisterActivityLifecycleCallbacks(
            ActivityLifecycleCallbacksCompat callback) {
        MainLifecycleDispatcher.get().unregisterActivityLifecycleCallbacks(
                callback);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void postIcsUnregisterActivityLifecycleCallbacks(
            Application application, ActivityLifecycleCallbacksCompat callback) {
        application
                .unregisterActivityLifecycleCallbacks(new ActivityLifecycleCallbacksWrapper(
                        callback));
    }
}
