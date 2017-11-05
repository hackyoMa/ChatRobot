package com.chatrobot.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户语料库操作
 * Created by hackyo on 2017/5/22.
 */
public final class CorpusOperat {

    private final static String dir;

    static {
        dir = System.getProperty("user.dir") + System.getProperty("file.separator") + "corpus" + System.getProperty("file.separator");
    }

    //为新商户创建文件
    public static void addCorpus(int userId) {
        try {
            File corpus = new File(dir + userId + ".aiml");
            if (corpus.createNewFile()) {
                FileWriter writer = new FileWriter(corpus);
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><aiml></aiml>");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //查询所有拦截
    public static List<Map> selectCategory(int userId) {
        List<Map> contentList = new ArrayList<>();
        try {
            Document document = parse(userId);
            Element root = document.getRootElement();
            List categoryList = root.elements("category");
            for (Object obj : categoryList) {
                Element element = (Element) obj;
                String pattern = element.element("pattern").getText();
                String template = element.element("template").getText();
                Map<String, String> categoryMap = new HashMap<>();
                categoryMap.put("pattern", pattern);
                categoryMap.put("template", template);
                contentList.add(categoryMap);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return contentList;
    }

    //修改拦截
    public static String modifyCategory(int userId, String newPattern, String newTemplate, String originalPattern) {
        try {
            Document document = parse(userId);
            Element root = document.getRootElement();
            Element pattern = (Element) root.selectSingleNode("//pattern[text()='" + originalPattern + "']");
            pattern.setText(newPattern);
            Element template = pattern.getParent().element("template");
            template.setText(newTemplate);
            writerFile(userId, document);
            return "success";
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "failure";
    }

    //添加一条拦截
    public static String addCategory(int userId, String pattern, String template) {
        try {
            Document document = parse(userId);
            Element root = document.getRootElement();
            Element patternEle = (Element) root.selectSingleNode("//pattern[text()='" + pattern + "']");
            if (patternEle == null) {
                Element category = root.addElement("category");
                category.addElement("pattern").setText(pattern);
                category.addElement("template").setText(template);
                writerFile(userId, document);
                return "success";
            } else {
                return "exist";
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "failure";
    }

    //删除一条拦截
    public static String removeCategory(int userId, String pattern) {
        try {
            Document document = parse(userId);
            Element root = document.getRootElement();
            Element patternEle = (Element) root.selectSingleNode("//pattern[text()='" + pattern + "']");
            root.remove(patternEle.getParent());
            writerFile(userId, document);
            return "success";
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "failure";
    }

    //返回用户语料库文件
    private static Document parse(int userId) throws DocumentException {
        return new SAXReader().read(dir + userId + ".aiml");
    }

    //写入文件
    private static void writerFile(int userId, Document document) {
        OutputFormat of = new OutputFormat("    ", true, "UTF-8");
        of.setTrimText(true);
        try {
            FileOutputStream fos = new FileOutputStream(dir + userId + ".aiml");
            XMLWriter xmlWriter = new XMLWriter(fos, of);
            xmlWriter.write(document);
            xmlWriter.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
