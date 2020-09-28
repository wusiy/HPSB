package com.sunland.cpocr.crash;

import android.app.Activity;
import android.os.Bundle;

public interface ActivityLifecycleCallbacksCompat {
    void onActivityCreated(Activity activity, Bundle savedInstanceState);

    void onActivityStarted(Activity activity);

    void onActivityResumed(Activity activity);

    void onActivityPaused(Activity activity);

    void onActivityStopped(Activity activity);

    void onActivitySaveInstanceState(Activity activity, Bundle outState);

    void onActivityDestroyed(Activity activity);
}
