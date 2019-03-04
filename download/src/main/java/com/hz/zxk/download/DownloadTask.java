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
import com.hz.zxk.download.util.NetworkUtils;

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
public class DownloadTask {

    private static DownloadTask sManager;

    //最大线程数
    private int maxThreadSize;
    private Context mContext;
    //总的下载进度
    private long totalProgress = 0;
    //一定时间段内下载的大小
    private long downloadSize = 0;
    //已完成下载的线程个数
    private AtomicInteger mCompleteThreadNum = new AtomicInteger(0);
    //下载路径
    private String mUrl;
    //下载回调
    private DownloadCallback mCallback;
    //下载速度
    String totalNetworkSpeed;
    private List<DownloadRunnable> mDownloadRunnables;

    private long networkSpeedTime;

    public DownloadTask() {
        this.mDownloadRunnables = new ArrayList<>();
    }

    public DownloadTask(Context context, int maxThreadSize, String url, DownloadCallback callback) {
        this();
        this.mContext = context;
        this.maxThreadSize = maxThreadSize;
        this.mUrl = url;
        this.mCallback = callback;
    }

    /**
     * 返回下载路径
     *
     * @return
     */
    public String getUrl() {
        return mUrl;
    }

    public static DownloadTask getInstance() {
        if (sManager == null) {
            synchronized (DownloadTask.class) {
                if (sManager == null) {
                    sManager = new DownloadTask();
                }
            }
        }
        return sManager;
    }

    /**
     * 开始下载
     */
    public void start() {
        //从数据库获取上次下载的进度，如果有，直接从上次下载终止的位置下载
        //如果没有，则从网络中获取文件的长度
        List<DownloadProgress> lastDownloadProgresses = DownloadDBManager.getInstance(mContext).query(mUrl);
        if (lastDownloadProgresses != null && lastDownloadProgresses.size() > 0) {
            //获取上次下载的总进度
            for (DownloadProgress lastDownloadProgress : lastDownloadProgresses) {
                totalProgress += lastDownloadProgress.getProgress();
            }
            lastProgressDownload(mContext, lastDownloadProgresses, mCallback);
        } else {
            HttpManager.getInstance().aSyncRequest(mUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (mCallback != null) {
                        mCallback.fail(ErrorCode.UNKOWN_ERROR_CODE, e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful() && mCallback != null) {
                        mCallback.fail(ErrorCode.NETWORK_ERROR_CODE, "网络错误");
                        return;
                    }
                    if (response.body() == null) {
                        mCallback.fail(ErrorCode.BODY_ERROR_CODE, "无法获取body内容");
                        return;
                    }
                    long contentLength = response.body().contentLength();
                    if (contentLength == -1 && mCallback != null) {
                        mCallback.fail(ErrorCode.CONTENT_LENGTH_ERROR_CODE, "无法获取文件长度");
                        return;
                    }
                    progressDownload(mContext, mUrl, contentLength, mCallback);
                }
            });
        }
    }

    /**
     * 再次下载上次下载未完成的部分
     *
     * @param context
     * @param downloadProgresses
     */
    private void lastProgressDownload(Context context, List<DownloadProgress> downloadProgresses, DownloadCallback callback) {
        for (int i = 0; i < downloadProgresses.size(); i++) {
            DownloadProgress downloadProgress = downloadProgresses.get(i);
            long startSize = downloadProgress.getStartSize() + downloadProgress.getProgress();
            long endSize = downloadProgress.getEndSize();
            startDownload(context, downloadProgress.getThreadId(), downloadProgress.getUrl(), downloadProgress.getFileName(), startSize, endSize
                    , downloadProgress.getContentLength(), downloadProgress.getProgress(), callback);
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
        long threadDownloadSize = contentLength / maxThreadSize;
        String fileName = url.substring(url.lastIndexOf("/"), url.length());
        for (int i = 0; i < maxThreadSize; i++) {
            String threadId = "threadId-" + (i + 1);
            long startSize = threadDownloadSize * i;
            long endSize = threadDownloadSize * (i + 1) - 1;
            startDownload(context, threadId, url, fileName, startSize, endSize, contentLength, 0, callback);
            //保存数据库
            //记录每条线程的下载的进度
            DownloadProgress downloadProgress = new DownloadProgress();
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
     *
     * @param context
     * @param url
     * @param startSize
     * @param endSize
     * @param contentLength
     * @param callback
     */
    private void startDownload(Context context, String threadId, String url, String fileName, long startSize, long endSize, final long contentLength, final long progress, final DownloadCallback callback) {
        DownloadRunnable runnable = new DownloadRunnable(context, threadId, url, fileName, startSize, endSize, progress, new DownloadCallback() {
            @Override
            public void success(File file) {
                mCompleteThreadNum.getAndIncrement();
                if (mCompleteThreadNum.get() == maxThreadSize) {
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
            public void stop(String url) {

            }

            @Override
            public void progress(long progress,String networkSpeed) {
                synchronized (DownloadTask.this) {
                    totalProgress += progress;
                    downloadSize += progress;
                    if (System.currentTimeMillis() - networkSpeedTime >= 1000) {
                        networkSpeedTime = System.currentTimeMillis();
                        totalNetworkSpeed= NetworkUtils.networkSpeed(downloadSize)+"/s";
                        downloadSize = 0;
                    }
                    if (callback != null) {
                        callback.progress((int) (Float.valueOf(String.valueOf(totalProgress)) / contentLength * 100),totalNetworkSpeed);
                    }
                }
            }

            @Override
            public void start() {

            }

            @Override
            public void pending() {

            }
        });
        //加入线程池
        DownloadDispatch.getInstance().getExcutorService().execute(runnable);
        mDownloadRunnables.add(runnable);
    }

    public void stopDownload() {
        Log.d("TAG", "downloadRunnables.size=" + mDownloadRunnables);
        for (DownloadRunnable downloadRunnable : mDownloadRunnables) {
            downloadRunnable.stop();
        }
        if (mCallback != null) {
            mCallback.stop(mUrl);
        }

    }


}
