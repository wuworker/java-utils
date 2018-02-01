package com.wxl.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2018/1/23.
 * json和xml互相转换
 */
public class JsonXmlConverterTest {


    @Test
    public void testToJson() {
        JsonXmlConverter jsonXmlConverter = new JsonXmlConverter();
        jsonXmlConverter.setContainAttr(true);
        jsonXmlConverter.setAttrPrefix("@!@");
        jsonXmlConverter.setDefaultJsonKey("vv");
        String xml = "<Envelope>" +
                "<Body>" +
                "<sayHi name=\"wxl\">hi!</sayHi>" +
                "<sayHi name=\"wxl\">hi2!</sayHi>" +
                "</Body>" +
                "</Envelope>";
        String json = jsonXmlConverter.xmlToJsonString(xml, true);
        System.out.println(json);

        jsonXmlConverter.setAttrPrefix("@");
        jsonXmlConverter.setDefaultJsonKey("v");
        json = jsonXmlConverter.xmlToJsonString(xml, true);
        System.out.println(json);

        xml = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "<soap:Body>\n" +
                "<wx:sayHello name=\"wxl\" xmlns:wx=\"http://service.wxl.com/HelloWorld\">\n" +
                "<arg0 age=\"13\">哈哈</arg0>\n" +
                "<arg1>123</arg1>\n" +
                "</wx:sayHello>\n" +
                "</soap:Body>\n" +
                "</soap:Envelope>";

        jsonXmlConverter.setContainNs(true);
        json = jsonXmlConverter.xmlToJsonString(xml, true);
        System.out.println(json);

        jsonXmlConverter.setContainNs(false);
        jsonXmlConverter.setContainAttr(false);
        json = jsonXmlConverter.xmlToJsonString(xml, true);
        System.out.println(json);
    }


    @Test
    public void testJsonToXmlMap1() {
        JsonXmlConverter converter = new JsonXmlConverter();
        Map<String, Object> json = new HashMap<>();

        System.out.println("单个元素:");
        json = JsonUtils.put(json, "a", "nice");
        String xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        System.out.println("setCreateRootAuto false:");
        converter.setCreateRootAuto(false);
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        //attr converter
        System.out.println("属性转换:");
        json.clear();
        json = JsonUtils.put(json,"a.@name","haha");
        System.out.println(JsonUtils.toPrettyFormat(json));
        converter.setCreateRootAuto(true);
        converter.setAttrPrefix("@");

        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        json = JsonUtils.put(json,"a.value","gg");
        System.out.println(JsonUtils.toPrettyFormat(json));
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        json = JsonUtils.put(json,"a.age","13");
        System.out.println(JsonUtils.toPrettyFormat(json));
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        json = JsonUtils.put(json,"a.@age","13");
        System.out.println(JsonUtils.toPrettyFormat(json));
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        json = JsonUtils.put(json,"a.@body.0","10");
        json = JsonUtils.put(json,"a.@body.1","11");
        System.out.println(JsonUtils.toPrettyFormat(json));
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);

        converter.setShowSequence(true);
        xml = converter.jsonToXmlString(json, true);
        System.out.println(xml);
    }

    @Test
    public void testJsonToXmlMap2() {
        JsonXmlConverter converter = new JsonXmlConverter();
        Map<String,Object> json = new HashMap<>();
        json.put("a","haha");
        json.put("b",13);
        System.out.println(JsonUtils.toPrettyFormat(json));
        String xml = converter.jsonToXmlString(json,true);
        System.out.println(xml);

        converter.setDefaultRoot("nice");
        converter.setCreateRootAuto(true);
        xml = converter.jsonToXmlString(json,true);
        System.out.println(xml);
    }

    @Test
    public void testJsonToXml3(){
        JsonXmlConverter converter = new JsonXmlConverter();
        Map<String,Object> json = new HashMap<>();
        json = JsonUtils.put(json,"user.info.name","哈哈");
        json = JsonUtils.put(json,"user.info.age",12);
        json = JsonUtils.put(json,"user.info.address.province","浙江");
        json = JsonUtils.put(json,"user.info.address.city","杭州");
        json = JsonUtils.put(json,"user.grade.0",98);
        json = JsonUtils.put(json,"user.grade.1",99);
        json = JsonUtils.put(json,"user.grade.2",97);
        json = JsonUtils.put(json,"@id","ajshdfuahfashdfuhahashsdaf");
        json = JsonUtils.put(json,"user.info.@id","adsfsdfsdfdsf");
        json = JsonUtils.put(json,"grade.0",98);
        json = JsonUtils.put(json,"grade.1",99);
        json = JsonUtils.put(json,"grade.2.name","gg");
        json = JsonUtils.put(json,"grade.2.@age","11");
        json = JsonUtils.put(json,"grade.3.0","哈哈");
        json = JsonUtils.put(json,"grade.3.2","ggggg");
        System.out.println(JsonUtils.toPrettyFormat(json));

        converter.setDefaultRoot("message");
        converter.setCreateRootAuto(true);
        converter.setAttrPrefix("@");
        converter.setContainAttr(true);
        converter.setShowSequence(false);
        converter.setListKeyPrefix("list");

        String xml = converter.jsonToXmlString(json,true);
        System.out.println(xml);

        converter.setDefaultJsonKey("value");
        String jsonStr = converter.xmlToJsonString(xml,true);
        System.out.println(jsonStr);
    }


    @Test
    public void testJsonToXmlList(){
        JsonXmlConverter converter = new JsonXmlConverter();

        //list
        List<Object> listJosn = new ArrayList<>();
        listJosn.add("a");
        listJosn.add("b");
        System.out.println(JsonUtils.toPrettyFormat(listJosn));
        String xml = converter.jsonToXmlString(listJosn,true);
        System.out.println(xml);


        converter.setDefaultRoot("nice");
        converter.setListKeyPrefix("");
        converter.setShowSequence(true);
        xml = converter.jsonToXmlString(listJosn,true);
        System.out.println(xml);


        converter.setCreateRootAuto(false);
        xml = converter.jsonToXmlString(listJosn,true);
        System.out.println(xml);

        converter.setCreateRootAuto(true);
        converter.setAttrPrefix("@");
        listJosn = JsonUtils.put(listJosn,"2.@name","haha");
        System.out.println(JsonUtils.toPrettyFormat(listJosn));
        xml = converter.jsonToXmlString(listJosn,true);
        System.out.println(xml);

        listJosn = JsonUtils.put(listJosn,"2.@age","13");
        listJosn = JsonUtils.put(listJosn,"2.value","123");
        System.out.println(JsonUtils.toPrettyFormat(listJosn));
        xml = converter.jsonToXmlString(listJosn,true);
        System.out.println(xml);
    }

}


