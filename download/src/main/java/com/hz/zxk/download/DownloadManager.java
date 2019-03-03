package com.hz.zxk.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hz.zxk.download.callback.DownloadCallback;
import com.hz.zxk.download.constants.ErrorCode;
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
                String contentType = response.body().contentType().type();
                progressDownload(context, url, contentLength, contentType, callback);
            }
        });
    }

    /**
     * 分配每个线程下载的长度
     *
     * @param url
     * @param contentLength
     * @param contentType
     * @param callback
     */
    private void progressDownload(Context context, String url, final long contentLength, String contentType, final DownloadCallback callback) {
        //分配每个线程下载长度
        long threadDownloadSize = contentLength / MAX_THREAD;
        for (int i = 0; i < MAX_THREAD; i++) {
            long startSize = threadDownloadSize * i;
            long endSize = threadDownloadSize * (i + 1) - 1;
            DownloadRunnable runnable = new DownloadRunnable(context, url, startSize, endSize, new DownloadCallback() {
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
    }

    public void stopDownload() {
        for (DownloadRunnable downloadRunnable : mDownloadRunnables) {
            downloadRunnable.stop();
        }
    }
}
