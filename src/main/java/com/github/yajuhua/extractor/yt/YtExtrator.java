package com.github.yajuhua.extractor.yt;


import com.github.yajuhua.extractor.yt.downloader.DefaultDownloader;
import com.github.yajuhua.extractor.yt.downloader.Downloader;

import java.net.Proxy;

public class YtExtrator {
    public static Proxy proxy;
    private static Downloader downloader;

    /**
     * 初始化时设置下载器(可选)
     * @param downloader
     */
    public static void init(final Downloader downloader){
        YtExtrator.downloader = downloader;
    }

    /**
     * 获取下载器
     * @return
     */
    public static Downloader getDownloader(){
        if (YtExtrator.downloader == null){
            //默认下载器
            return new DefaultDownloader();
        }
        return YtExtrator.downloader;
    }
}
