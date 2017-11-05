package com.chatrobot.utils;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.BatchSmsAttributes;
import com.aliyun.mns.model.MessageAttributes;
import com.aliyun.mns.model.RawTopicMessage;

/**
 * 短信发送
 * Created by hackyo on 2017/5/23.
 */
public final class SendSms {

    final private static String accessId = "VCrvYtHuKLZaIgHb";
    final private static String accessKey = "4nJ9VvKxSP7ToYEEwAUt5uLXc6fUxO";
    final private static String accessEndpoint = "http://1399816066890230.mns.cn-hangzhou.aliyuncs.com/";
    final private static String accessTopic = "sms.topic-cn-hangzhou";
    final private static String accessSignName = "小明智能导购机器人";
    final private static String accessReg = "SMS_93395002";

    //发送注册短信
    public static boolean sendVCode(String to, String vCode) {
        CloudAccount account = new CloudAccount(accessId, accessKey, accessEndpoint);
        MNSClient client = account.getMNSClient();
        CloudTopic topic = client.getTopicRef(accessTopic);
        RawTopicMessage msg = new RawTopicMessage();
        msg.setMessageBody("sms-message");
        MessageAttributes messageAttributes = new MessageAttributes();
        BatchSmsAttributes batchSmsAttributes = new BatchSmsAttributes();
        batchSmsAttributes.setFreeSignName(accessSignName);
        batchSmsAttributes.setTemplateCode(accessReg);
        BatchSmsAttributes.SmsReceiverParams smsReceiverParams = new BatchSmsAttributes.SmsReceiverParams();
        smsReceiverParams.setParam("vCode", vCode);
        batchSmsAttributes.addSmsReceiver(to, smsReceiverParams);
        messageAttributes.setBatchSmsAttributes(batchSmsAttributes);
        try {
            topic.publishMessage(msg, messageAttributes);
        } catch (ServiceException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.close();
        return true;
    }

}
