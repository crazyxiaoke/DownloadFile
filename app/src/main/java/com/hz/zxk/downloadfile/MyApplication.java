package com.hz.zxk.downloadfile;

import android.app.Application;

import com.hz.zxk.download.DownloadDispatch;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadDispatch.getInstance().init(this);
    }
}
