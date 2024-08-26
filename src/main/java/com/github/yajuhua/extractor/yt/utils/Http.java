package com.github.yajuhua.extractor.yt.utils;

import com.github.yajuhua.extractor.yt.YtExtrator;
import com.github.yajuhua.extractor.yt.downloader.Request;
import com.github.yajuhua.extractor.yt.downloader.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public class Http {

    /**
     * GET请求
     * @param url
     * @param params
     * @param heads
     * @return
     */
    public static String get(String url, Map params, Map heads){
        String Content = null;
        HttpURLConnection connection = null;
        try {
            //拼接参数
            if (params!= null && !params.isEmpty()){
                StringBuilder paramsStr = new StringBuilder(url);
                Set keySet = params.keySet();
                int count = 0;
                for (Object key : keySet) {
                    if (count == 0){
                        paramsStr.append("?" + key + "=" + params.get(key));
                    }else {
                        paramsStr.append("&" + key + "=" + params.get(key));
                    }
                    count++;
                }
                url = url + paramsStr;
            }
            if (YtExtrator.proxy == null){
                connection =(HttpURLConnection)new URL(url).openConnection();
            }else {
                connection =(HttpURLConnection)new URL(url).openConnection(YtExtrator.proxy);
            }

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 Edg/92.0.902.67");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
            connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
            connection.setRequestMethod("GET");
            if (heads != null){
                Set set = heads.keySet();
                for (Object key : set) {
                    connection.setRequestProperty((String) key, (String) heads.get(key));
                }
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            Content = "";
            while ((line = reader.readLine()) != null) {
                Content = Content + line;
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  Content;
    }

    /**
     * 发送POST请求
     * @param url 请求的URL（包含参数）
     * @param body 请求体参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, String body, Map<String, String> headers) {
        StringBuilder content = new StringBuilder();
        HttpURLConnection connection = null;

        try {
            // 创建连接
            if (YtExtrator.proxy != null) {
                connection = (HttpURLConnection) new URL(url).openConnection(YtExtrator.proxy);
            } else {
                connection = (HttpURLConnection) new URL(url).openConnection();
            }
            connection.setRequestMethod("POST");

            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 发送请求体
            if (body != null && !body.isEmpty()) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json"); // 设置Content-Type为JSON
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] bytes = body.getBytes("UTF-8");
                    os.write(bytes);
                    os.flush();
                }
            }

            // 读取响应内容
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // 打印异常信息
            throw new RuntimeException("Failed to send POST request", e);
        } finally {
            if (connection != null) {
                connection.disconnect(); // 断开连接
            }
        }
        return content.toString();
    }

    /**
     * 获取页面html字符串
     * @param url
     * @return
     * @throws Exception
     */
    public static String getHtml(String url) throws Exception {
        Request get = Request.builder()
                .httpMethod("GET")
                .url(url)
                .build();
        Response response = YtExtrator.getDownloader().execute(get);
        if (response.getResponseCode() != 200){
            throw new Exception(response.getResponseMessage());
        }
        return response.getResponseBody();
    }
}
