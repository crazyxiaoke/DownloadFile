package com.hz.zxk.downloadfile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.hz.zxk.download.DownloadManager;
import com.hz.zxk.download.callback.DownloadCallback;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button download, stopDownload;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        download = findViewById(R.id.download);
        stopDownload = findViewById(R.id.stop_download);
        mProgressBar = findViewById(R.id.progress);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "开始下载");
                DownloadManager.getInstance().download(getBaseContext(),
                        "http://gyxz.ukdj3d.cn/a31/rj_zmy1/jiangjuncaijing.apk",
                        new DownloadCallback() {
                            @Override
                            public void success(File file) {
                                Log.d("TAG", "下载完成");
                                Log.d("TAG", "存放路径=" + file.getAbsolutePath());
                            }

                            @Override
                            public void fail(int code, String errormsg) {
                                Log.d("TAG", "code=" + code + ",errormsg=" + errormsg);
                            }

                            @Override
                            public void progress(long progress) {
                                Log.d("TAG", "progress=" + progress);
                                mProgressBar.setProgress((int) progress);
                            }
                        });
            }
        });

        stopDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadManager.getInstance().stopDownload();
            }
        });
    }
}
