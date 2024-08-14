package com.github.yajuhua.extractor.yt.utils;

import org.testng.annotations.Test;

/**
 * 解析时间字符串
 */
public class ParseTime {
    /**
     * 转换成秒数
     * @param text 如 22:13:10
     * @return
     */
    public static Integer toSecond(String text){
        String[] parts = text.split(":");
        Integer seconds = 0;

        // 根据时间部分的数量进行解析
        if (parts.length == 3) { // 格式 "HH:mm:ss"
            seconds += Integer.parseInt(parts[0]) * 3600; // 小时转秒
            seconds += Integer.parseInt(parts[1]) * 60;   // 分钟转秒
            seconds += Integer.parseInt(parts[2]);        // 秒
        } else if (parts.length == 2) { // 格式 "mm:ss"
            seconds += Integer.parseInt(parts[0]) * 60;   // 分钟转秒
            seconds += Integer.parseInt(parts[1]);        // 秒
        } else if (parts.length == 1) { // 格式 "ss"
            seconds += Integer.parseInt(parts[0]);        // 秒
        }

        return seconds;
    }

    @Test
    public void test1(){
        System.out.println(toSecond("10"));
    }
}
