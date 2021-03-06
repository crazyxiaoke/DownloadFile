package com.hz.zxk.download.callback;

import java.io.File;

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
public interface DownloadCallback {

    /**
     * 下载开始
     */
    void start();

    /**
     * 下载等待
     */

    void pending();

    /**
     * 下载完成
     *
     * @param file
     */
    void success(File file);

    /**
     * 下载失败
     *
     * @param code
     * @param errormsg
     */
    void fail(int code, String errormsg);

    /**
     * 停止下载
     */
    void stop(String url);

    /**
     * 下载进度
     *
     * @param progress
     */
    void progress(long progress, String networkspeed);

}
