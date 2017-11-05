package com.chatrobot.utils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送
 * Created by hackyo on 2017/3/23.
 */
public final class SendEmail {

    private static Session session;

    static {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.transport.protocol", "smtp");
        session = Session.getDefaultInstance(props);
    }

    //发送注册邮件
    public static boolean sendVCode(String to, String vCode) {
        String content = "<div style=\"width: 600px;font: normal 1.2em/1.6em 'Microsoft YaHei','sans-serif';\">\n" +
                "    <p style=\"color: #F0AD4E;font-size: 2em;font-style: italic;margin: 0;\">小明智能导购机器人</p>\n" +
                "    <p style=\"color: #F0AD4E;font-size: 1em;font-style: italic;text-align: right;margin: 0;\">注册验证码</p>\n" +
                "    <div style=\"padding-top: 24px;background-color: #F0AD4E;\"></div>\n" +
                "    <p>尊敬的用户<a href=\"mailto: " + to + "\" target=\"_blank\">" + to + "</a>，您好！</p>\n" +
                "    <p>本次验证邮箱的验证码为：<span style=\"color: #F0AD4E;font-weight: bold;\">" + vCode + "</span>，请在30分钟内完成验证！</p>\n" +
                "    <p>感谢您注册小明智能导购机器人，我们将为您提供最便捷的服务！<br/>现在就使用吧！<a href=\"https://www.spicybar.cn/\" target=\"_blank\">spicybar.cn</a></p>\n" +
                "    <p style=\"text-align: right;\">小明&nbsp;敬启</p>\n" +
                "    <p style=\"font-size: 0.8em;font-weight: lighter;font-style: italic;\">此为系统自动发送邮件，请勿直接回复！</p>\n" +
                "    <div style=\"padding-top: 24px;background-color: #F0AD4E;\"></div>\n" +
                "</div>";
        return send(to, "注册验证码", content);
    }

    //发送推荐邮件
    public static boolean sendSub(String to, List<Map> goodsList) {
        if (goodsList.size() == 0) {
            return false;
        }
        StringBuilder goodsContent = new StringBuilder();
        for (Map goodsMap : goodsList) {
            goodsContent.append("&nbsp;&nbsp;&nbsp;&nbsp;<span>商品：<b>");
            goodsContent.append(goodsMap.get("name"));
            goodsContent.append("</b></span>&nbsp;&nbsp;&nbsp;&nbsp;<span>现价：<em>");
            goodsContent.append(goodsMap.get("price"));
            goodsContent.append("</em></span><br/>\n");
        }
        String content = "<div style=\"width: 600px;font: normal 1.2em/1.6em 'Microsoft YaHei','sans-serif';\">\n" +
                "    <p style=\"color: #F0AD4E;font-size: 2em;font-style: italic;margin: 0;\">小明智能导购机器人</p>\n" +
                "    <p style=\"color: #F0AD4E;font-size: 1em;font-style: italic;text-align: right;margin: 0;\">收藏内容推荐</p>\n" +
                "    <div style=\"padding-top: 24px;background-color: #F0AD4E;\"></div>\n" +
                "    <p>尊敬的用户<a href=\"mailto: " + to + "\" target=\"_blank\">" + to + "</a>，您好！</p>\n" +
                "    <p>您收藏的内容有更新了哦：</p>\n" +
                "    <p>" + goodsContent.toString() + "</p>\n" +
                "    <p>快去看看吧！<a href=\"https://www.spicybar.cn/\" target=\"_blank\">spicybar.cn</a></p>\n" +
                "    <p style=\"text-align: right;\">小明&nbsp;敬启</p>\n" +
                "    <p style=\"font-size: 0.8em;font-weight: lighter;font-style: italic;\">此为系统自动发送邮件，请勿直接回复！</p>\n" +
                "    <div style=\"padding-top: 24px;background-color: #F0AD4E;\"></div>\n" +
                "</div>";
        return send(to, "收藏内容推荐", content);
    }

    //发送邮件
    private static boolean send(String to, String subject, String content) {
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress("postmaster@spicybar.cn", "小明智能导购机器人", "UTF-8"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to, "尊敬的用户", "UTF-8"));
            message.setSubject(subject, "UTF-8");
            message.setContent(content, "text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();
            Transport transport = session.getTransport();
            transport.connect("postmaster@spicybar.cn", "6HCVrz1nvQQS6eSv");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

}
