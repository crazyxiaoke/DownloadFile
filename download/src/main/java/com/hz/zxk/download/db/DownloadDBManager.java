package com.hz.zxk.download.db;

import android.content.Context;

/**
 * 　　┏┓　　　　┏┓
 * 　┏┛┻━━━━┛┻┓
 * 　┃　　　　　　　　┃
 * 　┃　　　━　　　　┃
 * 　┃　┳┛　┗┳　　┃
 * 　┃　　　　　　　　┃
 * 　┃　　　┻　　　　┃
 * 　┃　　　　　　　　┃
 * 　┗━━┓　　　┏━┛
 * 　　　　┃　　　┃　　　神兽保佑
 * 　　　　┃　　　┃　　　代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * <p>
 * Created by zxk on 19-3-1.
 */
public class DownloadDBManager {
    private String db_name = "download_progress";
    private DaoMaster.DevOpenHelper mOpenHelper;

    private static DownloadDBManager sInstance;

    public DownloadDBManager(Context context) {
        mOpenHelper = new DaoMaster.DevOpenHelper(context, db_name);
    }

    public static DownloadDBManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DownloadDBManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadDBManager(context);
                }
            }
        }
        return sInstance;
    }



}
