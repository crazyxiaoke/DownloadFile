package com.hz.zxk.download.http;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
public class HttpManager {

    private static HttpManager sManager;

    private OkHttpClient mHttpClient;

    public HttpManager() {
        mHttpClient = new OkHttpClient();
    }

    public static HttpManager getInstance() {
        if (sManager == null) {
            synchronized (HttpManager.class) {
                if (sManager == null) {
                    sManager = new HttpManager();
                }
            }
        }
        return sManager;
    }

    /**
     * 异步请求
     *
     * @param url
     * @param callback
     */
    public void aSyncRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url).build();
        mHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 同步请求，使用Range定义每次获取的文件长度
     * @param url
     * @param startSize
     * @param endSize
     * @return
     */
    public Response syncRequestRange(String url, long startSize, long endSize) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", String.format("bytes=%s-%s", startSize, endSize))
                .build();
        try {
            return mHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
