package com.hz.zxk.downloadfile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hz.zxk.download.DownloadDispatch;
import com.hz.zxk.download.DownloadTask;
import com.hz.zxk.download.callback.DownloadCallback;
import com.hz.zxk.downloadfile.adapter.DownloadListAdapter;
import com.hz.zxk.downloadfile.entiy.ApkInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button download, download1, download2, download3;
    private TextView progressText, progressText1, progressText2, progressText3;
    private ProgressBar mProgressBar, mProgressBar1, mProgressBar2, mProgressBar3;
    private TextView networkSpeedText, networkSpeedText1, networkSpeedText2, networkSpeedText3;

    private RecyclerView mRecyclerView;
    private DownloadListAdapter mAdapter;

    //下载状态 0：下载，1：暂停下载
    private int downloadStatus = 0;
    private int downloadStatus1 = 0;
    private int downloadStatus2 = 0;
    private int downloadStatus3 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdapter = new DownloadListAdapter(this,this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        List<ApkInfo> datas = initData();
        mAdapter.refreshDatas(datas);
    }

    private List<ApkInfo> initData() {
        List<ApkInfo> list = new ArrayList<>();
        ApkInfo jjcj = new ApkInfo();
        jjcj.setName("将军财经");
        jjcj.setUrl("http://gyxz.ukdj3d.cn/a31/rj_zmy1/jiangjuncaijing.apk");
        list.add(jjcj);
        ApkInfo aqy = new ApkInfo();
        aqy.setName("爱奇异");
        aqy.setUrl("http://gyxz.ro4uw.cn/hk1/rj_gyc1/aiqiyihd.apk");
        list.add(aqy);
        ApkInfo ypgpt = new ApkInfo();
        ypgpt.setName("优品股票通");
        ypgpt.setUrl("http://gyxz.ro4uw.cn/hk1/rj_yx1/youpingupiaotong.apk");
        list.add(ypgpt);
        ApkInfo qbs = new ApkInfo();
        qbs.setName("七八社");
        qbs.setUrl("http://gyxz.ro4uw.cn/hk/rj_xb1/qibashe.apk");
        list.add(qbs);
        return list;
    }

}
