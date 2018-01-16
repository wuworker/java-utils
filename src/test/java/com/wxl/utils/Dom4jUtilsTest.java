package com.wxl.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2018/1/13.
 * dom4jTest
 */
public class Dom4jUtilsTest {

    @Test
    public void test()throws Exception{
        String xml = "<Envelope><Body><sayHi name=\"wxl\">hi!  </sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element body = envelope.element("Body");
        Element sayHi = body.element("sayHi");

        System.out.println(body.getText()+"|");
        System.out.println(body.getTextTrim()+"|");
        System.out.println(body.getStringValue()+"|");

        System.out.println("------------------------------------");

        System.out.println(sayHi.getText()+"|");
        System.out.println(sayHi.getTextTrim()+"|");
        System.out.println(sayHi.getStringValue()+"|");
    }

    @Test
    public void test2()throws Exception{
        String xml = "<a><b>nice</b><c>nice</c></a>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));
    }

    @Test
    public void getSafeChildElement() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl\">hi!</sayHi><sayHi name=\"wxl\">hi2!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element sayHi = Dom4jUtils.getSafeChildElement(envelope, "Body", "sayHi");
        System.out.println(sayHi.getText());

        try {
            Element notExist = Dom4jUtils.getSafeChildElement(envelope, "IBody", "sayHi");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getUnSafeChildElement() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl\">hi!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element sayHi = Dom4jUtils.getUnSafeChildElement(envelope, "Body", "sayHi");
        System.out.println(sayHi.getText());


        Element notExist = Dom4jUtils.getUnSafeChildElement(envelope, "IBody", "sayHi");
        System.out.println(notExist == null);
    }

    @Test
    public void getSafeAttributeValue() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl\">hi!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element sayHi = Dom4jUtils.getSafeChildElement(envelope, "Body", "sayHi");
        String v = Dom4jUtils.getSafeAttributeValue(sayHi, "name");
        System.out.println(v);

        try {
            Dom4jUtils.getSafeAttributeValue(sayHi, "age");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getUnSafeAttributeValue() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl\">hi!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element sayHi = Dom4jUtils.getSafeChildElement(envelope, "Body", "sayHi");
        String v = Dom4jUtils.getUnSafeAttributeValue(sayHi, "name");
        System.out.println(v);

        v = Dom4jUtils.getUnSafeAttributeValue(sayHi, "age");
        System.out.println(v == null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findSafeEleByAttrEquals() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl1\">hi!</sayHi><sayHi name=\"wxl2\">hi2!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element body = Dom4jUtils.getSafeChildElement(envelope, "Body");
        Element sayHi = Dom4jUtils.findSafeEleByAttrEquals(body.elements("sayHi"), "name", "wxl2");
        System.out.println(sayHi.getText());

        try {
            Dom4jUtils.findSafeEleByAttrEquals(body.elements("sayHi"), "name", "wxl3");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findUnSafeEleByAttrEquals() throws Exception {
        String xml = "<Envelope><Body><sayHi name=\"wxl1\">hi!</sayHi><sayHi name=\"wxl2\">hi2!</sayHi></Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element body = Dom4jUtils.getSafeChildElement(envelope, "Body");
        Element sayHi = Dom4jUtils.findUnSafeEleByAttrEquals(body.elements("sayHi"), "name", "wxl2");
        System.out.println(sayHi.getText());

        sayHi = Dom4jUtils.findUnSafeEleByAttrEquals(body.elements("sayHi"), "name", "wxl3");
        System.out.println(sayHi == null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findUnSafeElesByAttrEquals() throws Exception {
        String xml = "<Envelope><Body>" +
                "<sayHi name=\"wxl1\">hi!</sayHi>" +
                "<sayHi name=\"wxl2\">hi2!</sayHi>" +
                "<sayHi name=\"wxl2\">hi2!</sayHi>" +
                "</Body></Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        Document document = DocumentHelper.parseText(xml);
        Element envelope = document.getRootElement();
        Element body = Dom4jUtils.getSafeChildElement(envelope, "Body");
        List<Element> sayHis = Dom4jUtils.findElesByAttrEquals(body.elements("sayHi"), "name", "wxl2");
        System.out.println(sayHis.size());

        sayHis = Dom4jUtils.findElesByAttrEquals(body.elements("sayHi"), "name", "wxl3");
        System.out.println(sayHis.size());
    }

    @Test
    public void xmlToJson() throws Exception {
        String xml1 = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<soap:Body>\n" +
                "<wx:sayHello name=\"wxl\" xmlns:wx=\"http://service.wxl.com/HelloWorld\">\n" +
                "<arg0 age=\"13\">哈哈</arg0>\n" +
                "<arg1>123</arg1>\n" +
                "</wx:sayHello>\n" +
                "</soap:Body>\n" +
                "</soap:Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml1));

        String json1 = Dom4jUtils.xmlToJsonString(xml1, false, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");

        json1 = Dom4jUtils.xmlToJsonString(xml1, true, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");

        json1 = Dom4jUtils.xmlToJsonString(xml1, true, true);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");

        json1 = Dom4jUtils.xmlToJsonString(xml1, false, true);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");
    }

    @Test
    public void xmlToJson2() throws Exception {
        String xml1 = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<soap:Body>\n" +
                "<wx:sayHello name=\"wxl\" xmlns:wx=\"http://service.wxl.com/HelloWorld\">\n" +
                "<arg0 age=\"13\">哈哈</arg0>\n" +
                "<arg0>123</arg0>\n" +
                "</wx:sayHello>\n" +
                "</soap:Body>\n" +
                "</soap:Envelope>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml1));

        String json1 = Dom4jUtils.xmlToJsonString(xml1, false, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");

        json1 = Dom4jUtils.xmlToJsonString(xml1, true, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");
    }

    @Test
    public void xmlToJson3() throws Exception {
        String xml1 = "<root>" +
                "<group address=\"gugong\">" +
                "<name age=\"123\">haha</name>" +
                "<name>hehe</name>" +
                "<name>gg</name>" +
                "</group>" +
                "<group address=\"yiheyuan\">" +
                "<name>haha</name>" +
                "<name>hehe</name>" +
                "</group>" +
                "</root>";
        System.out.println(Dom4jUtils.toPrettyFormat(xml1));

        String json1 = Dom4jUtils.xmlToJsonString(xml1, false, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");

        json1 = Dom4jUtils.xmlToJsonString(xml1, true, false);
        System.out.println(json1);
        System.out.println("-------------------------------------------------------------");
    }


    @Test
    public void testJsonToXml()throws Exception {
        Map<String,Object> json = new HashMap<>();
        json = JsonUtils.put(json,"a.0","nice");
        json = JsonUtils.put(json,"a.1","nice");
        String jsonStr = JsonUtils.toPrettyFormat(json);
        System.out.println(jsonStr);

        String xml = Dom4jUtils.jsonToXmlString(jsonStr);
        System.out.println(xml);

        List<Object> listJson = new ArrayList<>();
        listJson.add("nice");
        listJson.add("haha");
        listJson = JsonUtils.put(listJson,"2.0","hehe");
        listJson = JsonUtils.put(listJson,"2.1","hehehehe");
        String listJsonStr = JsonUtils.toPrettyFormat(listJson);
        System.out.println(listJsonStr);

        String xml2 = Dom4jUtils.jsonToXmlString(listJsonStr);
        System.out.println(xml2);
    }


    @Test
    public void testJsonToXml2()throws Exception {
        Map<String,Object> json = new HashMap<>();
        json = JsonUtils.put(json,"a.0","nice1");
        json = JsonUtils.put(json,"a.1","nice2");
        json = JsonUtils.put(json,"a.2.0","nice3");
        json = JsonUtils.put(json,"a.2.1","nice4");
        json = JsonUtils.put(json,"a.3.c1","nice5");
        json = JsonUtils.put(json,"a.3.c2","nice6");
        json = JsonUtils.put(json,"b.e1","haha1");
        json = JsonUtils.put(json,"b.e2","haha2");
        json = JsonUtils.put(json,"b.e3.0","haha3");
        json = JsonUtils.put(json,"b.e3.2","haha4");
        String jsonStr = JsonUtils.toPrettyFormat(json);
        System.out.println(jsonStr);


        String xml = Dom4jUtils.jsonToXmlString(jsonStr,"root");
        System.out.println(Dom4jUtils.toPrettyFormat(xml));

        String json2 = Dom4jUtils.xmlToJsonString(xml);
        System.out.println(json2);
    }

    @Test
    public void testJsonToXml3()throws Exception {
        List<Object> json = new ArrayList<>();
        json = JsonUtils.put(json,"0","nice1");
        json = JsonUtils.put(json,"1","nice2");
        json = JsonUtils.put(json,"2.0","nice3");
        json = JsonUtils.put(json,"2.1","nice4");
        json = JsonUtils.put(json,"3.c1","nice5");
        json = JsonUtils.put(json,"3.c2","nice6");
        json = JsonUtils.put(json,"4.e1","haha1");
        json = JsonUtils.put(json,"4.e2","haha2");
        json = JsonUtils.put(json,"4.e3.0","haha3");
        json = JsonUtils.put(json,"4.e3.2","haha4");
        String jsonStr = JsonUtils.toPrettyFormat(json);
        System.out.println(jsonStr);


        String xml = Dom4jUtils.jsonToXmlString(jsonStr);
        System.out.println(xml);
    }


}















