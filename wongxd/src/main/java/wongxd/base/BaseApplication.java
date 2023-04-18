package wongxd.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


/**
 * Created by wongxd on 2018/06/11.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public abstract class BaseApplication extends Application {

    public static Application appInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = getInstance();
        //注册监听每个activity的生命周期,便于堆栈式管理
        registerActivityLifecycleCallbacks(mCallbacks);
    }

    /**
     * 获得当前app运行的AppContext
     */
    public abstract Application getInstance();

    private ActivityLifecycleCallbacks mCallbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            AppManager.getAppManager().addActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            AppManager.getAppManager().removeActivity(activity);
        }
    };
}
