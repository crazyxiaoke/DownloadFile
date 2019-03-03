package com.hz.zxk.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hz.zxk.download.callback.DownloadCallback;
import com.hz.zxk.download.constants.ErrorCode;
import com.hz.zxk.download.db.DownloadDBManager;
import com.hz.zxk.download.db.DownloadProgress;
import com.hz.zxk.download.http.HttpManager;
import com.hz.zxk.download.runnable.DownloadRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
public class DownloadManager {

    private static DownloadManager sManager;

    private static final int MAX_THREAD = 5;

    //总的下载进度
    private long totalProgress = 0;
    //已完成下载的线程个数
    private AtomicInteger mCompleteThreadNum = new AtomicInteger(0);

    private List<DownloadRunnable> mDownloadRunnables;

    public DownloadManager() {
        this.mDownloadRunnables = new ArrayList<>();
    }

    public static DownloadManager getInstance() {
        if (sManager == null) {
            synchronized (DownloadManager.class) {
                if (sManager == null) {
                    sManager = new DownloadManager();
                }
            }
        }
        return sManager;
    }

    //创建线程池
    private static ThreadPoolExecutor sThreadPool = new ThreadPoolExecutor(MAX_THREAD, MAX_THREAD,
            60, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadFactory() {
        //使用AtomicInteger来实现自增操作，线程安全，
        //如果使用i++或++i非线程安全
        private AtomicInteger mInteger = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "download thread#" + mInteger.getAndIncrement());
        }
    });

    public void init(){

    }

    /**
     * 下载文件
     *
     * @param url
     * @param callback
     */
    public void download(final Context context, final String url, final DownloadCallback callback) {
        //从数据库获取上次下载的进度，如果有，直接从上次下载终止的位置下载
        //如果没有，则从网络中获取文件的长度
        List<DownloadProgress> lastDownloadProgresses=DownloadDBManager.getInstance(context).query(url);
        if(lastDownloadProgresses!=null&&lastDownloadProgresses.size()>0){
            //获取上次下载的总进度
            for (DownloadProgress lastDownloadProgress : lastDownloadProgresses) {
                totalProgress+=lastDownloadProgress.getProgress();
            }
            lastProgressDownload(context,lastDownloadProgresses,callback);
        }else{
            HttpManager.getInstance().aSyncRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (callback != null) {
                        callback.fail(ErrorCode.UNKOWN_ERROR_CODE, e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() && callback != null) {
                        callback.fail(ErrorCode.NETWORK_ERROR_CODE, "网络错误");
                        return;
                    }
                    if (response.body() == null) {
                        callback.fail(ErrorCode.BODY_ERROR_CODE, "无法获取body内容");
                        return;
                    }
                    long contentLength = response.body().contentLength();
                    if (contentLength == -1 && callback != null) {
                        callback.fail(ErrorCode.CONTENT_LENGTH_ERROR_CODE, "无法获取文件长度");
                        return;
                    }
                    progressDownload(context, url, contentLength, callback);
                }
            });
        }
    }

    /**
     * 再次下载上次下载未完成的部分
     * @param context
     * @param downloadProgresses
     */
    private void lastProgressDownload(Context context,List<DownloadProgress> downloadProgresses,DownloadCallback callback){
        for(int i=0;i<downloadProgresses.size();i++){
            DownloadProgress downloadProgress=downloadProgresses.get(i);
            long startSize=downloadProgress.getProgress();
            long endSize=downloadProgress.getEndSize();
            startDownload(context,downloadProgress.getThreadId(),downloadProgress.getUrl(),downloadProgress.getFileName(),startSize,endSize,
                    downloadProgress.getProgress(),downloadProgress.getContentLength(),callback);
        }
    }

    /**
     * 分配每个线程下载的长度
     *
     * @param url
     * @param contentLength
     * @param callback
     */
    private void progressDownload(Context context, String url, final long contentLength, final DownloadCallback callback) {
        //分配每个线程下载长度
        long threadDownloadSize = contentLength / MAX_THREAD;
        String fileName = url.substring(url.lastIndexOf("/"), url.length());
        for (int i = 0; i < MAX_THREAD; i++) {
           String threadId="threadId-"+(i+1);
           long startSize=threadDownloadSize * i;
           long endSize=threadDownloadSize * (i+1) - 1;
           startDownload(context,threadId,url,fileName,startSize,endSize,contentLength,0,callback);
           //保存数据库
            //记录每条线程的下载的进度
            DownloadProgress downloadProgress=new DownloadProgress();
            downloadProgress.setUrl(url);
            downloadProgress.setContentLength(contentLength);
            downloadProgress.setFileName(fileName);
            downloadProgress.setThreadId(threadId);
            downloadProgress.setStartSize(startSize);
            downloadProgress.setEndSize(endSize);
            downloadProgress.setProgress(0);
            DownloadDBManager.getInstance(context).save(downloadProgress);
        }
    }

    /**
     * 开始下载
     * @param context
     * @param url
     * @param startSize
     * @param endSize
     * @param contentLength
     * @param callback
     */
    private void startDownload(Context context,String threadId, String url,String fileName, long startSize, long endSize, final long contentLength,final long progress, final DownloadCallback callback){
        DownloadRunnable runnable = new DownloadRunnable(context,threadId, url,fileName, startSize, endSize,progress, new DownloadCallback() {
            @Override
            public void success(File file) {
                mCompleteThreadNum.getAndIncrement();
                if (mCompleteThreadNum.get() == MAX_THREAD) {
                    if (callback != null) {
                        callback.success(file);
                    }
                }
            }

            @Override
            public void fail(int code, String errormsg) {
                if (callback != null) {
                    callback.fail(code, errormsg);
                }
                //有一个线程报错，就停止所有线程下载
                stopDownload();
            }

            @Override
            public void progress(long progress) {
                synchronized (DownloadManager.this) {
                    totalProgress += progress;
                    if (callback != null) {
                        callback.progress((int) (Float.valueOf(String.valueOf(totalProgress)) / contentLength * 100));
                    }
                }
            }
        });
        sThreadPool.execute(runnable);
        mDownloadRunnables.add(runnable);
    }

    public void stopDownload() {
        for (DownloadRunnable downloadRunnable : mDownloadRunnables) {
            downloadRunnable.stop();
        }
    }
}
