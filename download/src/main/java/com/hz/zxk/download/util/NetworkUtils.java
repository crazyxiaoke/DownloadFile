package com.hz.zxk.download.util;

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
 * Created by zxk on 19-3-4.
 */
public class NetworkUtils {
    /**
     * 字节转换
     *
     * @param b
     * @return
     */
    public static String networkSpeed(long b) {
        if (b < 1024) {
            return b + "Byte";
        } else if (b < 1024 * 1024) {
            return b / 1024 + "KB";
        } else if (b < 1024 * 1024 * 1024) {
            return ((float) (b * 100 / 1024 / 1024 * 1024)) / 100 + "MB";
        }
        return "0Byte";
    }
}
