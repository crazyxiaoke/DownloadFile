package com.hz.zxk.downloadfile;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.hz.zxk.download.DownloadDispatch;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        DownloadDispatch.getInstance().init(this);
    }
}
