package com.chatrobot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qcloud.image.ImageClient;
import com.qcloud.image.request.TagDetectRequest;

import static com.qcloud.image.http.RequestBodyKey.BUCKET;

/**
 * 图像识别
 * Created by 13712 on 2017/6/16.
 */
public final class ImageAnalysis {

    //物体识别
    public static String identify(byte[] tagImage) {
        ImageClient imageClient = new ImageClient(1251268258, "AKID7HKKF9MRozynRBra7cw40raAh7WnpEQZ", "EIBAHNWaZN3tfJ0umpqrtrv9MRM69GRm");
        TagDetectRequest tagReq = new TagDetectRequest(BUCKET, tagImage);
        String ret = imageClient.tagDetect(tagReq);
        System.out.println(ret);
        JSONObject resultSet = JSON.parseObject(ret);
        String result = null;
        if ((Integer) resultSet.get("code") == 0) {
            JSONArray tags = (JSONArray) resultSet.get("tags");
            int confidence = 0;
            int index = 0;
            for (int i = 0; i < tags.size(); i++) {
                int tagConfidence = (int) ((JSONObject) tags.get(i)).get("tag_confidence");
                if (tagConfidence >= confidence) {
                    confidence = tagConfidence;
                    index = i;
                }
            }
            result = (String) ((JSONObject) tags.get(index)).get("tag_name");
        }
        imageClient.shutdown();
        return result;
    }

}
