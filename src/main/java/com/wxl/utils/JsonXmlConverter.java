package com.wxl.utils;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuxingle on 2018/1/23.
 * json,xml的互相转换
 * xml使用dom4j
 * json使用fastjson
 */
@Data
public class JsonXmlConverter {

    private static final String DEFAULT_ATTR_PREFIX = "@";

    private static final String DEFAULT_JSON_KEY = "value";

    private static final String DEFAULT_ROOT_NAME = "root";

    private static final String DEFAULT_LIST_KEY_PREFIX = "list";

    //是否包含命名空间
    private boolean containNs;

    //是否包含属性
    private boolean containAttr;

    //属性前缀标识
    private String attrPrefix;

    /**
     * 当出现只有value没有key时,使用这个默认的key
     * 比如:
     * <a name="nice">haha</a>
     * 转json后
     * {
     * "a":{
     * "@name":"nice",
     * "defaultJsonKey":"haha"
     * }
     * }
     */
    private String defaultJsonKey;


    //默认的根节点
    private String defaultRoot;

    //是否自动创建根节点
    private boolean createRootAuto;

    /**
     * 当输入的json为List时
     * ["a","b"]
     * 转换后
     * <ListKeyPrefix>a</ListKeyPrefix>
     * <ListKeyPrefix>b</ListKeyPrefix>
     */
    private String listKeyPrefix;
    //如果是list时转成element后，是否显示顺序
    private boolean showSequence;


    public JsonXmlConverter() {
        containNs = false;
        containAttr = true;
        attrPrefix = DEFAULT_ATTR_PREFIX;
        defaultJsonKey = DEFAULT_JSON_KEY;

        defaultRoot = DEFAULT_ROOT_NAME;
        createRootAuto = true;
        listKeyPrefix = DEFAULT_LIST_KEY_PREFIX;
        showSequence = false;
    }


    /**
     * xml字符串转json对象
     */
    public Map<String, Object> xmlToJson(String xml) {
        Assert.hasText(xml, "input text must be xml text");
        try {
            Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            return xmlToJson(rootElement);
        } catch (DocumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * xml对象转json对象
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> xmlToJson(Element element) {
        Assert.notNull(element, "element can not null");
        String name = containNs ? element.getQualifiedName() : element.getName();
        List<Attribute> attributes = element.attributes();
        List<Element> elements = element.elements();
        Map<String, Object> json = new LinkedHashMap<>();
        //element没有子节点并且(不包含属性或者属性为空)
        if (elements.isEmpty() && (!containAttr || attributes.isEmpty())) {
            json.put(name, element.getTextTrim());
        } else {
            Map<String, Object> child = new LinkedHashMap<>();
            if (containAttr) {
                for (Attribute attr : attributes) {
                    child.put(attrPrefix + (containNs ? attr.getQualifiedName() : attr.getName()), attr.getValue());
                }
                if (elements.isEmpty()) {
                    child.put(StringUtils.isEmpty(defaultJsonKey) ? DEFAULT_JSON_KEY : defaultJsonKey, element.getTextTrim());
                }
            }
            for (Element ele : elements) {
                //如果element有同名的组成list
                Map<String, Object> map = xmlToJson(ele);
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
     * xml字符串转json字符串
     */
    public String xmlToJsonString(String xml) {
        return JSON.toJSONString(xmlToJson(xml));
    }

    public String xmlToJsonString(String xml, boolean format) {
        return JSON.toJSONString(xmlToJson(xml), format);
    }

    /**
     * xml对象转json字符串
     */
    public String xmlToJsonString(Element element) {
        return JSON.toJSONString(xmlToJson(element));
    }

    public String xmlToJsonString(Element element, boolean format) {
        return JSON.toJSONString(xmlToJson(element), format);
    }

    /**
     * json字符串转xml对象
     * 自动创建根节点
     */
    public Element jsonToXml(String jsonStr) {
        Object json = JSON.parse(jsonStr);
        return jsonToXml(json);
    }

    /**
     * json对象转xml对象
     * 自动创建根节点
     */
    public Element jsonToXml(Object json) {
        return jsonToXml(json, null);
    }

    /**
     * json字符串转xml字符串
     */
    public String jsonToXmlString(String jsonStr) {
        Object json = JSON.parse(jsonStr);
        return jsonToXmlString(json);
    }

    public String jsonToXmlString(String jsonStr, boolean format) {
        Object json = JSON.parse(jsonStr);
        return jsonToXmlString(json, format);
    }

    /**
     * json对象转xml字符串
     */
    @SuppressWarnings("unchecked")
    public String jsonToXmlString(Object json) {
        return jsonToXmlString(json, false);
    }

    @SuppressWarnings("unchecked")
    public String jsonToXmlString(Object json, boolean format) {
        List<Element> elements;
        if (createRootAuto) {
            Element root = jsonToXml(json, null);
            elements = new ArrayList<>();
            elements.add(root);
        } else {
            String rootName = "defaultRoot";
            Element root = DocumentHelper.createElement(rootName);
            root = jsonToXml(json, root);
            elements = root.elements();
        }
        if (format) {
            XMLWriter writer = null;
            try {
                OutputFormat formater = OutputFormat.createPrettyPrint();
                StringWriter out = new StringWriter();
                writer = new XMLWriter(out, formater);
                for (Element e : elements) {
                    writer.write(e);
                }
                return out.toString();
            } catch (IOException e) {
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
        } else {
            StringBuilder sb = new StringBuilder();
            for (Element e : elements) {
                sb.append(e.asXML());
            }
            return sb.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private Element jsonToXml(Object json, Element root) {
        if (json instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) json;
            if (root == null) {
                //map里只有一个,那么这个就是根节点
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
                        if (key.startsWith(attrPrefix)) {
                            root = DocumentHelper.createElement(defaultRoot);
                            root.addAttribute(key.substring(attrPrefix.length()), value == null ? "" : value.toString());
                        } else {
                            root = DocumentHelper.createElement(key);
                            root.setText(value == null ? "" : value.toString());
                        }
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
            jsonListToXml((List<Object>) json, root, listKeyPrefix);
        } else {
            throw new IllegalArgumentException("input obj is not a json");
        }
        return root;
    }


    @SuppressWarnings("unchecked")
    private void jsonMapToXml(Map<String, Object> json, Element root) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
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
                if (key.startsWith(attrPrefix)) {
                    root.addAttribute(key.substring(attrPrefix.length()), value == null ? "" : value.toString());
                } else {
                    valueMap.put(key, value);
                }
            }
        }
        //只有1个是value,其他都是属性
        if (valueMap.size() == 1 && json.size() > valueMap.size()) {
            Object value = valueMap.values().iterator().next();
            root.setText(value == null ? "" : value.toString());
        } else {
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                Element sub = DocumentHelper.createElement(entry.getKey());
                sub.setText(entry.getValue() == null ? "" : entry.getValue().toString());
                root.add(sub);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void jsonListToXml(List<Object> json, Element root, String key) {
        for (int i = 0; i < json.size(); i++) {
            String k = showSequence ? key + i : key;
            Object obj = json.get(i);
            if (obj instanceof Map) {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(k) ? String.valueOf(i) : k);
                root.add(element);
                jsonMapToXml((Map<String, Object>) obj, element);
            } else if (obj instanceof List) {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(k) ? String.valueOf(i) : k);
                root.add(element);
                jsonListToXml((List<Object>) obj, element, StringUtils.isEmpty(k) ? listKeyPrefix : k);
            } else {
                Element element = DocumentHelper.createElement(StringUtils.isEmpty(k) ? String.valueOf(i) : k);
                element.setText(obj == null ? "" : obj.toString());
                root.add(element);
            }
        }
    }


}
