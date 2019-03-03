package com.hz.zxk.download.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

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
    private Context context;
    private static DownloadDBManager sInstance;

    public DownloadDBManager(Context context) {
        this.context=context;
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

    /**
     * 获取可读数据库
     * @return
     */
    public SQLiteDatabase getReadableDatabase(){
        if(mOpenHelper==null){
            mOpenHelper=new DaoMaster.DevOpenHelper(context,db_name);
        }
        return mOpenHelper.getReadableDatabase();
    }

    /**
     * 获取可读写数据库
     * @return
     */
    public SQLiteDatabase getWritableDatabase(){
        if(mOpenHelper==null){
            mOpenHelper=new DaoMaster.DevOpenHelper(context,db_name);
        }
        return mOpenHelper.getWritableDatabase();
    }

    /**
     * 根据url查询上次下载的进度
     * @param url
     * @return
     */
    public List<DownloadProgress> query(String url){
        DaoMaster daoMaster=new DaoMaster(getReadableDatabase());
        DaoSession daoSession=daoMaster.newSession();
        DownloadProgressDao downloadProgressDao=daoSession.getDownloadProgressDao();
        QueryBuilder<DownloadProgress> qb=downloadProgressDao.queryBuilder();
        qb.where(DownloadProgressDao.Properties.Url.eq(url));
        return qb.list();
    }

    /**
     * 保存当前线程下载进度
     * @param downloadProgress
     */
    public void save(DownloadProgress downloadProgress){
        DaoMaster daoMaster=new DaoMaster(getWritableDatabase());
        DaoSession daoSession=daoMaster.newSession();
        DownloadProgressDao downloadProgressDao=daoSession.getDownloadProgressDao();
        downloadProgressDao.save(downloadProgress);
    }

    /**
     * 更新下载进度
     * @param downloadProgress
     */
    public void update(DownloadProgress downloadProgress){
        DaoMaster daoMaster=new DaoMaster(getWritableDatabase());
        DaoSession daoSession=daoMaster.newSession();
        DownloadProgressDao downloadProgressDao=daoSession.getDownloadProgressDao();
        downloadProgressDao.update(downloadProgress);
    }

    /**
     * 删除下载进度
     * @param downloadProgress
     */
    public void delete(DownloadProgress downloadProgress){
        DaoMaster daoMaster=new DaoMaster(getWritableDatabase());
        DaoSession daoSession=daoMaster.newSession();
        DownloadProgressDao downloadProgressDao=daoSession.getDownloadProgressDao();
        downloadProgressDao.delete(downloadProgress);
    }


}
