package com.sunland.cpocr.crash;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ActivityLifecycleCallbacksWrapper implements
        Application.ActivityLifecycleCallbacks {

    private ActivityLifecycleCallbacksCompat mCallback;

    public ActivityLifecycleCallbacksWrapper(
            ActivityLifecycleCallbacksCompat callback) {
        this.mCallback = callback;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        mCallback.onActivityCreated(activity, bundle);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mCallback.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mCallback.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mCallback.onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mCallback.onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        mCallback.onActivitySaveInstanceState(activity, bundle);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mCallback.onActivityDestroyed(activity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ActivityLifecycleCallbacksWrapper that = (ActivityLifecycleCallbacksWrapper) o;

        if (mCallback != null ? !mCallback.equals(that.mCallback)
                : that.mCallback != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return mCallback != null ? mCallback.hashCode() : 0;
    }
}