本项目用于文件下载，支持多任务下载，断点下载。
问题：还需添加下载开始状态、待下载状态。  
1：初始化  
&emsp;在Application中初始化
```
    public class MyApplication extends Application{

        @Override
        public void onCreate() {
            super.onCreate();
            DownloadDispatch.getInstance().init(this);
        }

    }
```
2、启动下载
```
    DownloadDispatch.getInstance().download("http://gyxz.ukdj3d.cn/a31/rj_zmy1/jiangjuncaijing.apk",
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
                                public void stop(String url) {
                                    Log.d("TAG","暂停下载");
                                }

                                @Override
                                public void progress(long progress) {
                                    Log.d("TAG", "progress=" + progress);
                                    mProgressBar.setProgress((int) progress);
                                }
                            });

3、暂停下载
```
DownloadDispatch.getInstance().stop("http://gyxz.ukdj3d.cn/a31/rj_zmy1/jiangjuncaijing.apk");
```

