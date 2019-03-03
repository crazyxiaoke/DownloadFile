package com.hz.zxk.download.runnable;

import android.content.Context;
import android.util.Log;

import com.hz.zxk.download.callback.DownloadCallback;
import com.hz.zxk.download.constants.ErrorCode;
import com.hz.zxk.download.http.HttpManager;
import com.hz.zxk.download.util.FileStorageUtil;

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
    private String mUrl;
    private long mStartSize;
    private long mEndSize;
    private DownloadCallback mCallback;
    private long progress;
    //下载状态
    private int mStatus = STATUS_DOWNLOAD;

    public DownloadRunnable(Context context, String url, long startSize, long endSize, DownloadCallback callback) {
        this.mContext = context;
        this.mUrl = url;
        this.mStartSize = startSize;
        this.mEndSize = endSize;
        this.mCallback = callback;
    }

    //开始下载
    @Override
    public void run() {
        Response response = HttpManager.getInstance().syncRequestRange(mUrl, mStartSize, mEndSize);
        if (response == null && mCallback != null) {
            mCallback.fail(ErrorCode.UNKOWN_ERROR_CODE, "未知错误");
        } else if (!response.isSuccessful() && mCallback != null) {
            mCallback.fail(ErrorCode.NETWORK_ERROR_CODE, "网络错误");
        } else if (response.body() == null && mCallback != null) {
            mCallback.fail(ErrorCode.BODY_ERROR_CODE, "无法获取body");
        } else {
            String fileName = mUrl.substring(mUrl.lastIndexOf("/"), mUrl.length());
            File file = FileStorageUtil.getFile(mContext, fileName);
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
                    progress += len;
                    mCallback.progress(len);
                }
                if (progress >= (mEndSize - mStartSize) + 1) {
                    mCallback.success(file);
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
