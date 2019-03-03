package com.hz.zxk.download.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

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
@Entity
public class DownloadProgress {

    @Id(autoincrement = true)
    private long id;

    @Unique
    private String url;

    private String fileName;

    private String threadId;

    private long startSize;

    private long endSize;

    private long contentLength;

    private long progress;

    @Generated(hash = 625750760)
    public DownloadProgress(long id, String url, String fileName, String threadId,
            long startSize, long endSize, long contentLength, long progress) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.threadId = threadId;
        this.startSize = startSize;
        this.endSize = endSize;
        this.contentLength = contentLength;
        this.progress = progress;
    }

    @Generated(hash = 441206902)
    public DownloadProgress() {
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStartSize() {
        return startSize;
    }

    public void setStartSize(long startSize) {
        this.startSize = startSize;
    }

    public long getEndSize() {
        return endSize;
    }

    public void setEndSize(long endSize) {
        this.endSize = endSize;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThreadId() {
        return this.threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
