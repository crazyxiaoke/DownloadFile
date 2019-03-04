package com.hz.zxk.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.hz.zxk.download.callback.DownloadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadDispatch {
    //默认最大下载数
    private static final int DEFAULT_MAX_DOWNLAOD_SIZE = 3;
    //默认每个下载器开启的线程数
    private static final int DEFAULT_MAX_THREAD = 4;

    private Context context;
    //最大下载数
    private int maxDownlaodSize = DEFAULT_MAX_DOWNLAOD_SIZE;
    //每个下载起开启的线程数
    private int maxThreadSize = DEFAULT_MAX_THREAD;
    //线程池
    private ThreadPoolExecutor executorService;

    private static DownloadDispatch sDownlaodDispatch;

    /**
     * 待下载列表
     */
    private List<DownloadTask> readyDownloads = new ArrayList<>();
    /**
     * 正在下载列表
     */
    private List<DownloadTask> runningDownloads = new ArrayList<>();

    private Map<String, DownloadTask> runningDownloadMap = new HashMap<>();

    public static DownloadDispatch getInstance() {
        if (sDownlaodDispatch == null) {
            synchronized (DownloadDispatch.class) {
                if (sDownlaodDispatch == null) {
                    sDownlaodDispatch = new DownloadDispatch();
                }
            }
        }
        return sDownlaodDispatch;
    }


    public void init(Context context) {
        this.context = context;
    }

    public ThreadPoolExecutor getExcutorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(maxThreadSize * maxDownlaodSize, maxThreadSize * maxDownlaodSize, 60, TimeUnit.MICROSECONDS,
                    new LinkedBlockingDeque<Runnable>(), new ThreadFactory() {
                //使用AtomicInteger来实现自增操作，线程安全，
                //如果使用i++或++i非线程安全
                private AtomicInteger mInteger = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "download thread#" + mInteger.getAndIncrement());
                }
            });
        }
        return executorService;
    }

    /**
     * 开始下载
     *
     * @param url
     * @param downloadCallback
     */
    public void download(final String url, final DownloadCallback downloadCallback) {
        if (context == null) {
            throw new AndroidRuntimeException("请在Application中初始化,DownloadDispatch.init(this)");
        }
        DownloadTask downloadTask = new DownloadTask(context, maxThreadSize, url, new DownloadCallback() {
            @Override
            public void start() {

            }

            @Override
            public void pending() {

            }

            @Override
            public void success(File file) {
                //下载完成后，如果待下载列表中有下载任务，把待下载任务加入到正在下载列表中，开始下载
                removeRunningDownloads(url);
                nextStartDownload();
                if (downloadCallback != null) {
                    downloadCallback.success(file);
                }
            }

            @Override
            public void fail(int code, String errormsg) {
                //如果有一个下载失败，则下载下一个
                nextStartDownload();
                if (downloadCallback != null) {
                    downloadCallback.fail(code, errormsg);
                }
            }

            @Override
            public void stop(String url) {

            }

            @Override
            public void progress(long progress,String networkSpeed) {
                if (downloadCallback != null) {
                    downloadCallback.progress(progress,networkSpeed);
                }
            }
        });
        if (runningDownloads.size() < maxDownlaodSize) {
            //如果下载数量还没达到最大下载数，就直接进行下载
            Log.d("TAG", "开始下载");
            runningDownloads.add(downloadTask);
            downloadTask.start();
        } else {
            Log.d("TAG", "等待下载");
            //如果下载数量达到最大下载数，把下载任务放入待下载列表，等待下载
            downloadCallback.pending();
            readyDownloads.add(downloadTask);
        }
    }

    /**
     * 把readyDonwloads列表中的第一个下载任务加入到runningDownloads中
     * 并执行下载
     */
    private synchronized void nextStartDownload() {
        if (readyDownloads.size() > 0) {
            DownloadTask downloadTask = readyDownloads.get(0);
            runningDownloads.add(downloadTask);
            downloadTask.start();
            readyDownloads.remove(0);
        }
    }

    private void removeRunningDownloads(String url) {
        Iterator<DownloadTask> it = runningDownloads.iterator();
        while (it.hasNext()) {
            DownloadTask downloadTask = it.next();
            if (downloadTask.getUrl().equals(url)) {
                downloadTask.stopDownload();
                it.remove();
                break;
            }
        }
    }


    /**
     * 暂停下载任务
     *
     * @param url
     */
    public void stop(String url) {
        removeRunningDownloads(url);
    }

    /**
     * 暂停所有下载
     */
    private void stopAll() {
        for (DownloadTask runningDownload : runningDownloads) {
            runningDownload.stopDownload();
        }
    }
}
