package com.wxl.utils;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

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
     * 如果是属性，则在key前加个@区分
     * 如果是
     * <name age="12">haha</key>
     * 则默认haha的key为value
     * {
     * "name":{
     * "@age":"12"
     * "value":"haha"
     * }
     * }
     */
    public static Map<String, Object> xmlToJson(String xml) {
        return xmlToJson(xml, false, false);
    }

    public static String xmlToJsonString(String xml) {
        return JsonUtils.toPrettyFormat(xmlToJson(xml, false, false));
    }

    public static Map<String, Object> xmlToJson(String xml, boolean containAttr) {
        return xmlToJson(xml, containAttr, false);
    }

    public static String xmlToJsonString(String xml, boolean containAttr) {
        return JsonUtils.toPrettyFormat(xmlToJson(xml, containAttr, false));
    }

    public static Map<String, Object> xmlToJson(String xml, boolean containAttr, boolean containNs) {
        Assert.hasText(xml, "input text must be xml text");
        try {
            Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            return xmlToJson(rootElement, containAttr, containNs);
        } catch (DocumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String xmlToJsonString(String xml, boolean containAttr, boolean containNs) {
        return JsonUtils.toPrettyFormat(xmlToJson(xml, containAttr, containNs));
    }

    /**
     * @param containAttr 是否包含属性
     * @param containNs   是否包含前缀
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> xmlToJson(Element element, boolean containAttr, boolean containNs) {
        Assert.notNull(element, "element can not null");
        String name = containNs ? element.getQualifiedName() : element.getName();
        List<Attribute> attributes = element.attributes();
        List<Element> elements = element.elements();
        Map<String, Object> json = new LinkedHashMap<>();
        if (elements.isEmpty() && (!containAttr || attributes.isEmpty())) {
            json.put(name, element.getTextTrim());
        } else {
            Map<String, Object> child = new LinkedHashMap<>();
            if (containAttr) {
                for (Attribute attr : attributes) {
                    child.put("@" + (containNs ? attr.getQualifiedName() : attr.getName()), attr.getValue());
                }
                if (elements.isEmpty()) {
                    child.put("value", element.getTextTrim());
                }
            }
            for (Element ele : elements) {
                //如果element有同名的组成list
                Map<String, Object> map = xmlToJson(ele, containAttr, containNs);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Object old = child.get(key);
                    if (old == null) {
                        child.put(key, value);
                    } else {
                        if (old instanceof List) {
                            List<Object> list = (List<Object>) old;
                            list.add(value);
                        } else {
                            List<Object> list = new ArrayList<>();
                            list.add(old);
                            list.add(value);
                            child.put(key, list);
                        }
                    }
                }
            }
            json.put(name, child);
        }
        return json;
    }


    /**
     * json转xml
     */
    public static Element jsonToXml(String jsonStr, String defaultRoot) {
        Object obj = JsonUtils.parse(jsonStr);
        return jsonToXml(obj, defaultRoot);
    }

    public static Element jsonToXml(Object json, String defaultRoot) {
        return jsonToXml(json, null, defaultRoot);
    }

    public static String jsonToXmlString(String jsonStr) {
        Object obj = JsonUtils.parse(jsonStr);
        return jsonToXmlString(obj);
    }

    @SuppressWarnings("unchecked")
    public static String jsonToXmlString(Object json) {
        //生成默认的根节点
        String uniqueRoot = RandomUtils.generateUUID(true);
        Element root = jsonToXml(json, null, uniqueRoot);
        //返回时去掉生成的根节点
        if (root.getName().equals(uniqueRoot)) {
            StringBuilder sb = new StringBuilder();
            List<Element> elements = root.elements();
            for (Element e : elements) {
                sb.append(e.asXML());
            }
            return sb.toString();
        }
        return root.asXML();
    }

    public static String jsonToXmlString(String jsonStr, String defaultRoot) {
        Object obj = JsonUtils.parse(jsonStr);
        return jsonToXmlString(obj, defaultRoot);
    }

    public static String jsonToXmlString(Object json, String defaultRoot) {
        Element root = jsonToXml(json, null, defaultRoot);
        return root.asXML();
    }


    /**
     * xml转json
     *
     * @param json        json对象
     * @param root        根节点,json的key都设置在根节点下
     * @param defaultRoot 当root为null时,使用的默认根节点名字
     */
    @SuppressWarnings("unchecked")
    private static Element jsonToXml(Object json, Element root, String defaultRoot) {
        Assert.hasText(defaultRoot, "default root name can not empty");
        if (json instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) json;
            if (root == null) {
                if (map.size() == 1) {
                    Map.Entry<String, Object> entry = map.entrySet().iterator().next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Map) {
                        root = DocumentHelper.createElement(key);
                        jsonMapToXml((Map<String, Object>) value, root);
                    } else if (value instanceof List) {
                        root = DocumentHelper.createElement(defaultRoot);
                        jsonListToXml((List<Object>) value, root, key);
                    } else {
                        root = DocumentHelper.createElement(key);
                        jsonToXml(value, root, defaultRoot);
                    }
                } else {
                    root = DocumentHelper.createElement(defaultRoot);
                    jsonMapToXml(map, root);
                }
            } else {
                jsonMapToXml(map, root);
            }
        } else if (json instanceof List) {
            if (root == null) {
                root = DocumentHelper.createElement(defaultRoot);
            }
            jsonListToXml((List<Object>) json, root, null);
        } else {
            throw new IllegalArgumentException("input obj is not a json");
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    private static void jsonMapToXml(Map<String, Object> json, Element root) {
        for (Map.Entry<String, Object> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                Element element = DocumentHelper.createElement(key);
                root.add(element);
                jsonMapToXml((Map<String, Object>) value, element);
            } else if (value instanceof List) {
                jsonListToXml((List<Object>) value, root, key);
            } else {
                Element element = DocumentHelper.createElement(key);
                element.setText(value == null ? "" : value.toString());
                root.add(element);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void jsonListToXml(List<Object> json, Element root, String key) {
        for (int i = 0; i < json.size(); i++) {
            Object obj = json.get(i);
            if (obj instanceof Map) {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(key) ? String.valueOf(i) : key);
                root.add(element);
                jsonMapToXml((Map<String, Object>) obj, element);
            } else if (obj instanceof List) {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(key) ? String.valueOf(i) : key);
                root.add(element);
                jsonListToXml((List<Object>) obj, element, StringUtils.isEmpty(key) ? null : key);
            } else {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(key) ? String.valueOf(i) : key);
                element.setText(obj == null ? "" : obj.toString());
                root.add(element);
            }
        }
    }

}


