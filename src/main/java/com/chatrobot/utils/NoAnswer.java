package com.chatrobot.utils;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 转发图灵机器人
 * Created by hackyo on 2017/4/14.
 */
public final class NoAnswer {

    //过滤4字节字符
    private static String filterEmoji(String content) {
        byte[] conByte = content.getBytes();
        for (int i = 0; i < conByte.length; i++) {
            if ((conByte[i] & 0xF8) == 0xF0) {
                for (int j = 0; j < 4; j++) {
                    conByte[i + j] = 0x30;
                }
                i += 3;
            }
        }
        content = new String(conByte);
        return content.replaceAll("0000", "");
    }

    //编码请求的数据，解码返回的数据
    public static String organizeMessage(String text, String userId) {
        Map<String, String> content = new HashMap<>();
        content.put("key", "c57819f2080f44da9036f352fbe283dd");
        content.put("info", text);
        content.put("userid", userId);
        String result = null;
        String param = JSON.toJSONString(content);
        Map resultMap = JSON.parseObject(sendPost(param), Map.class);
        if ((int) resultMap.get("code") == 100000) {
            result = filterEmoji((String) resultMap.get("text"));
            if (result.contains("图灵")) {
                result = "我一直在学习中，问我点别的吧!";
            }
        }
        return result;
    }

    //发起POST请求，参数为param
    private static String sendPost(String param) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("http://www.tuling123.com/openapi/api").openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(50000);
            conn.setReadTimeout(50000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "token");
            conn.setRequestProperty("tag", "htc_new");
            conn.connect();
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

}
