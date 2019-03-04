package com.hz.zxk.download.runnable;

import android.content.Context;

import com.hz.zxk.download.callback.DownloadCallback;
import com.hz.zxk.download.constants.ErrorCode;
import com.hz.zxk.download.db.DownloadDBManager;
import com.hz.zxk.download.db.DownloadProgress;
import com.hz.zxk.download.http.HttpManager;
import com.hz.zxk.download.util.FileStorageUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

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
public class DownloadRunnable implements Runnable {
    //启动下载
    private static final int STATUS_DOWNLOAD = 1;
    //停止下载
    private static final int STATUS_STOP = 2;

    private Context mContext;
    /** 线程ID */
    private String mThreadId;
    /** 下载路径 */
    private String mUrl;
    /** 下载文件名 */
    private String mFileName;
    /** 下载开始位置 */
    private long mStartSize;
    /** 下载结束位置 */
    private long mEndSize;
    /** 下载回调 */
    private DownloadCallback mCallback;
    /** 下载进度 */
    private long mProgress;
    /** 下载状态 */
    private int mStatus = STATUS_DOWNLOAD;

    public DownloadRunnable(Context context,String threadId, String url,String fileName, long startSize, long endSize,
                            long lastProgress,DownloadCallback callback) {
        this.mContext = context;
        this.mThreadId=threadId;
        this.mFileName=fileName;
        this.mUrl = url;
        this.mStartSize = startSize;
        this.mEndSize = endSize;
        this.mProgress=lastProgress;
        this.mCallback = callback;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public void run() {
        //开始下载
        Response response = HttpManager.getInstance().syncRequestRange(mUrl, mStartSize, mEndSize);
        if (response == null && mCallback != null) {
            mCallback.fail(ErrorCode.UNKOWN_ERROR_CODE, "未知错误");
        } else if (!response.isSuccessful() && mCallback != null) {
            mCallback.fail(ErrorCode.NETWORK_ERROR_CODE, "网络错误");
        } else if (response.body() == null && mCallback != null) {
            mCallback.fail(ErrorCode.BODY_ERROR_CODE, "无法获取body");
        } else {
            //获取数据库存储的记录
            DownloadProgress downloadProgress=DownloadDBManager.getInstance(mContext).queryByThread(mUrl,mThreadId);
            File file = FileStorageUtils.getFile(mContext, mFileName);
            RandomAccessFile randomAccessFile = null;
            InputStream inputStream = response.body().byteStream();
            try {
                //使用多线程下载文件需要把File转换成RandomAccessFile
                //使用seek来指定文件插入位置
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(mStartSize);
                byte[] buff = new byte[1024];
                int len;
                while ((len = inputStream.read(buff, 0, buff.length)) != -1) {
                    if (mStatus == STATUS_STOP) {
                        break;
                    }
                    randomAccessFile.write(buff, 0, len);
                    mProgress += len;
                    mCallback.progress(len,null);
                    //更新数据库，更新下载进度
                    downloadProgress.setProgress(mProgress);
                    DownloadDBManager.getInstance(mContext).update(downloadProgress);
                }
                if (mProgress >= (downloadProgress.getEndSize() - downloadProgress.getStartSize()) + 1) {
                    mCallback.success(file);
                    //线程下载完成，删除数据库中这条线程的记录
                    DownloadDBManager.getInstance(mContext).delete(downloadProgress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        mStatus = STATUS_STOP;
    }
}
