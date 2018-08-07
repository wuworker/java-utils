package com.wxl.utils.base;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by wuxingle on 2018/1/13.
 * dom4j工具类
 */
public class Dom4jUtils {


    public static String toPrettyFormat(String xml) {
        XMLWriter writer = null;
        try {
            Document doc = DocumentHelper.parseText(xml);
            OutputFormat formater = OutputFormat.createPrettyPrint();
            StringWriter out = new StringWriter();
            writer = new XMLWriter(out, formater);
            writer.write(doc);
            return out.toString();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }


    /**
     * 获取非null子节点,没有则抛异常
     */
    public static Element getSafeChildElement(Element parent, String... keys) {
        Element cur = parent;
        for (int index = 0, len = keys.length; index < len; index++) {
            if (cur == null) {
                if (index == 0) {
                    throw new IllegalArgumentException("input element is null");
                }
                throw new IllegalArgumentException("get '" + keys[index - 1] + "' element is null");
            }
            cur = cur.element(keys[index]);
        }
        return cur;
    }

    /**
     * 获取子节点,没有则返回null
     */
    public static Element getUnSafeChildElement(Element parent, String... keys) {
        if (parent == null) {
            return null;
        }
        Element cur = parent;
        for (String key : keys) {
            cur = cur.element(key);
            if (cur == null) {
                return null;
            }
        }
        return cur;
    }


    /**
     * 获取element的属性值
     *
     * @param name 属性名
     * @return 属性值
     */
    public static Attribute getSafeAttribute(Element element, String name) {
        Assert.notNull(element, "element can not null");
        Attribute attribute = element.attribute(name);
        if (attribute == null) {
            throw new IllegalArgumentException("element '" + element.getName() + "' attr '" + name + "' is null");
        }
        return attribute;
    }

    public static String getSafeAttributeValue(Element element, String name) {
        Attribute attribute = getSafeAttribute(element, name);
        String value = attribute.getValue();
        if (value == null) {
            throw new IllegalArgumentException("element '" + element.getName() + "' attr '" + name + "' value is null");
        }
        return value;
    }

    public static String getUnSafeAttributeValue(Element element, String name) {
        if (element == null) {
            return null;
        }
        Attribute attribute = element.attribute(name);
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    /**
     * 在list中搜索到值为value的element
     *
     * @return 找到的第一个
     */
    public static Element findSafeEleByValueEquals(List<Element> list, String value) {
        Assert.notNull(list, "can not find element where value=" + value + ",because input elements is null");
        Element find = null;
        for (Element e : list) {
            if (Objects.equals(e.getText(), value)) {
                find = e;
                break;
            }
        }
        if (find == null) {
            throw new IllegalArgumentException("can not find element where value=" + value);
        }
        return find;
    }

    public static Element findUnSafeEleByValueEquals(List<Element> list, String value) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Element find = null;
        for (Element e : list) {
            if (Objects.equals(e.getText(), value)) {
                find = e;
                break;
            }
        }
        return find;
    }

    /**
     * 在list中搜索属性名为attrName,值为attrValue的element
     *
     * @return 找到的element集合
     */
    public static List<Element> findElesByValueEquals(List<Element> list, String value) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Element> result = new ArrayList<>();
        for (Element e : list) {
            if (Objects.equals(e.getText(), value)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * 在list中搜索属性名为attrName,值为attrValue的element
     *
     * @param list      待搜素的list
     * @param attrName  属性名
     * @param attrValue 属性值
     * @return 找到的第一个element
     */
    public static Element findSafeEleByAttrEquals(List<Element> list, String attrName, String attrValue) {
        Assert.notNull(list, "can not find element where attr=" + attrName
                + ",value=" + attrValue + ",because input elements is null");
        Element find = null;
        for (Element e : list) {
            Attribute attr = e.attribute(attrName);
            if (attr != null && Objects.equals(attrValue, attr.getValue())) {
                find = e;
                break;
            }
        }
        if (find == null) {
            throw new IllegalArgumentException("can not find element where attr=" + attrName + ", value=" + attrValue);
        }
        return find;
    }

    public static Element findUnSafeEleByAttrEquals(List<Element> list, String attrName, String attrValue) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Element find = null;
        for (Element e : list) {
            Attribute attr = e.attribute(attrName);
            if (attr != null && Objects.equals(attrValue, attr.getValue())) {
                find = e;
                break;
            }
        }
        return find;
    }


    /**
     * 在list中搜索属性名为attrName,值为attrValue的element
     *
     * @param list      待搜素的list
     * @param attrName  属性名
     * @param attrValue 属性值
     * @return 找到的element集合
     */
    public static List<Element> findElesByAttrEquals(List<Element> list, String attrName, String attrValue) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<Element> result = new ArrayList<>();
        for (Element e : list) {
            Attribute attr = e.attribute(attrName);
            if (attr != null && Objects.equals(attrValue, attr.getValue())) {
                result.add(e);
            }
        }
        return result;
    }


    /**
     * xml转json
     */
    public static Map<String, Object> xmlToJson(String xml) {
        JsonXmlConverter converter = new JsonXmlConverter();
        return converter.xmlToJson(xml);
    }

    public static Map<String, Object> xmlToJson(Element xml) {
        JsonXmlConverter converter = new JsonXmlConverter();
        return converter.xmlToJson(xml);
    }

    public static String xmlToJsonString(String xml) {
        JsonXmlConverter converter = new JsonXmlConverter();
        return converter.xmlToJsonString(xml);
    }

    public static String xmlToJsonString(Element xml) {
        JsonXmlConverter converter = new JsonXmlConverter();
        return converter.xmlToJsonString(xml);
    }

    public static Map<String, Object> xmlToJson(String xml, boolean containAttr) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setContainAttr(containAttr);
        return converter.xmlToJson(xml);
    }

    public static Map<String, Object> xmlToJson(Element xml, boolean containAttr) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setContainAttr(containAttr);
        return converter.xmlToJson(xml);
    }

    public static String xmlToJsonString(String xml, boolean containAttr) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setContainAttr(containAttr);
        return converter.xmlToJsonString(xml);
    }

    public static String xmlToJsonString(Element xml, boolean containAttr) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setContainAttr(containAttr);
        return converter.xmlToJsonString(xml);
    }

    /**
     * json转xml
     */
    public static Element jsonToXml(String jsonStr, String defaultRoot) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setDefaultRoot(defaultRoot);
        return converter.jsonToXml(jsonStr);
    }

    public static Element jsonToXml(Object json, String defaultRoot) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setDefaultRoot(defaultRoot);
        return converter.jsonToXml(json);
    }

    public static String jsonToXmlString(String jsonStr) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setCreateRootAuto(false);
        return converter.jsonToXmlString(jsonStr);
    }

    public static String jsonToXmlString(Object json) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setCreateRootAuto(false);
        return converter.jsonToXmlString(json);
    }

    public static String jsonToXmlString(String jsonStr, String defaultRoot) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setCreateRootAuto(true);
        converter.setDefaultRoot(defaultRoot);
        return converter.jsonToXmlString(jsonStr);
    }

    public static String jsonToXmlString(Object json, String defaultRoot) {
        JsonXmlConverter converter = new JsonXmlConverter();
        converter.setCreateRootAuto(true);
        converter.setDefaultRoot(defaultRoot);
        return converter.jsonToXmlString(json);
    }

}


