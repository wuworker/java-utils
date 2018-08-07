package com.wxl.utils.http.soap;

import com.wxl.utils.http.HttpUtils;
import com.wxl.utils.http.impl.SimpleHttpUtils;
import org.dom4j.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.wxl.utils.http.soap.WsdlParser.Binding.*;
import static com.wxl.utils.http.soap.WsdlParser.Message.*;
import static com.wxl.utils.http.soap.WsdlParser.MessageElement.NAME_ATTR;
import static com.wxl.utils.http.soap.WsdlParser.MessageElement.TYPE_ATTR;
import static com.wxl.utils.http.soap.WsdlParser.Operation.*;
import static com.wxl.utils.http.soap.WsdlParser.PortType.PORT_TYPE_ELEMENT;
import static com.wxl.utils.http.soap.WsdlParser.PortType.PORT_TYPE_NAME_ATTR;
import static com.wxl.utils.http.soap.WsdlParser.Service.*;
import static com.wxl.utils.http.soap.WsdlParser.Types.*;


/**
 * Created by wuxingle on 2018/1/11.
 * wsdl解析器
 */
public class WsdlParser {

    //attribute
    private static final String TARGET_NAMESPACE_ATTR = "targetNamespace";


    public static WsdlResult parseFromURL(String url) throws WsdlParserException {
        if (StringUtils.isEmpty(url) || !url.endsWith("?wsdl")) {
            throw new WsdlParserException("input url must be wsdl url");
        }
        try {
            HttpUtils httpUtils = SimpleHttpUtils.createDefault();
            byte[] bytes = httpUtils.doGet(url);
            return parseFromText(new String(bytes));
        } catch (Exception e) {
            throw new WsdlParserException(e);
        }
    }

    public static WsdlResult parseFromText(String wsdlText) throws WsdlParserException {
        Document document;
        try {
            document = DocumentHelper.parseText(wsdlText);
        } catch (DocumentException e) {
            throw new WsdlParserException(e);
        }

        Element definitions = document.getRootElement();
        WsdlResult wsdlResult = new WsdlResult();

        parseTargetNameSpace(definitions, wsdlResult);
        parseService(definitions, wsdlResult);
        parseBinding(definitions, wsdlResult);
        parsePortType(definitions, wsdlResult);
        parseMessage(definitions, wsdlResult);
        parseTypes(definitions, wsdlResult);
        parseVersion(definitions, wsdlResult);

        return wsdlResult;
    }

    /**
     * 解析命名空间
     */
    private static void parseTargetNameSpace(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        wsdlResult.targetNamespace = getSafeAttributeValue(definitions, TARGET_NAMESPACE_ATTR);
    }


    /**
     * 解析service
     */
    private static void parseService(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        Service serviceResult = new Service();

        Element service = getSafeChildElement(definitions, SERVICE_ELEMENT);
        serviceResult.name = getSafeShortAttributeValue(service, SERVICE_NAME_ATTR);

        Element port = getSafeChildElement(service, PORT_ELEMENT);
        serviceResult.portName = getSafeShortAttributeValue(port, PORT_NAME_ATTR);
        serviceResult.portBinding = getSafeShortAttributeValue(port, PORT_BINDING_ATTR);

        Element address = getSafeChildElement(port, ADDRESS_ELEMENT);
        serviceResult.address = getSafeAttributeValue(address, LOCATION_ATTR);

        wsdlResult.service = serviceResult;
    }

    /**
     * 解析binding
     */
    @SuppressWarnings("unchecked")
    private static void parseBinding(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        Binding bindingResult = new Binding();
        bindingResult.name = wsdlResult.service.portBinding;
        Element binding = findSafeEleByAttrEquals(definitions.elements(BINDING_ELEMENT),
                BINDING_NAME_ATTR, bindingResult.name);

        bindingResult.type = getSafeShortAttributeValue(binding, BINDING_TYPE_ATTR);

        Element subBinding = getSafeChildElement(binding, SUB_BINDING_ELEMENT);
        bindingResult.transport = getSafeShortAttributeValue(subBinding, TRANSPORT_ATTR);
        bindingResult.style = getSafeShortAttributeValue(subBinding, STYLE_ATTR);

        wsdlResult.service.binding = bindingResult;
        wsdlResult.binding = bindingResult;
    }

    /**
     * 解析portType
     */
    @SuppressWarnings("unchecked")
    private static void parsePortType(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        PortType portTypeResult = new PortType();
        portTypeResult.name = wsdlResult.service.binding.type;

        Element portType = findSafeEleByAttrEquals(definitions.elements(PORT_TYPE_ELEMENT),
                PORT_TYPE_NAME_ATTR, portTypeResult.name);

        List<Element> operations = portType.elements(OPERATION_ELEMENT);
        List<Operation> ops = new ArrayList<>(operations.size());
        for (Element op : operations) {
            Operation operation = new Operation();
            operation.name = getSafeShortAttributeValue(op, OPERATION_NAME_ATTR);
            Element input = op.element(INPUT_ELEMENT);
            Element output = op.element(OUTPUT_ELEMENT);
            if (input != null) {
                Message message = new Message();
                message.name = getSafeShortAttributeValue(input, MESSAGE_ATTR);
                operation.inputMessage = message;
            }
            if (output != null) {
                Message message = new Message();
                message.name = getSafeShortAttributeValue(output, MESSAGE_ATTR);
                operation.outputMessage = message;
            }
            ops.add(operation);
        }
        portTypeResult.operations = ops;

        wsdlResult.operations = ops;
        wsdlResult.service.binding.portType = portTypeResult;
        wsdlResult.portType = portTypeResult;
    }

    /**
     * 解析message
     */
    @SuppressWarnings("unchecked")
    private static void parseMessage(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        List<Element> messages = definitions.elements(MESSAGE_ELEMENT);
        List<Message> list = new ArrayList<>(messages.size() * 2);
        for (Operation op : wsdlResult.operations) {
            Message input = op.inputMessage;
            Message output = op.outputMessage;
            if (input != null) {
                Element findMsg = findSafeEleByAttrEquals(messages, MESSAGE_NAME_ATTR, input.name);
                input.element = getSafeShortAttributeValue(getSafeChildElement(findMsg, MESSAGE_PART_ELEMENT), PART_ELEMENT_ARRT);
                list.add(input);
            }
            if (output != null) {
                Element findMsg = findSafeEleByAttrEquals(messages, MESSAGE_NAME_ATTR, output.name);
                output.element = getSafeShortAttributeValue(getSafeChildElement(findMsg, MESSAGE_PART_ELEMENT), PART_ELEMENT_ARRT);
                list.add(output);
            }
        }
        wsdlResult.messages = list;
    }

    /**
     * 解析Types
     */
    @SuppressWarnings("unchecked")
    private static void parseTypes(Element definitions, WsdlResult wsdlResult) throws WsdlParserException {
        Element schema = getSafeChildElement(definitions, TYPES_ELEMENT, SCHEMA_ELEMENT);
        List<Message> list = wsdlResult.messages;
        if (!parseImportIfNeed(definitions, list))
            parseSchema(schema, list);
    }

    /**
     * 解析import
     */
    private static boolean parseImportIfNeed(Element definitions, List<Message> messages) throws WsdlParserException {
        Element schema = getSafeChildElement(definitions, TYPES_ELEMENT, SCHEMA_ELEMENT);
        Element importEle = schema.element(IMPORT_ELEMENT);
        if (importEle == null) {
            return false;
        }
        String schemaUrl = getSafeAttributeValue(importEle, SCHEMA_LOCATION_ATTR);
        Element root;
        try {
            HttpUtils httpUtils = SimpleHttpUtils.createDefault();
            byte[] bytes = httpUtils.doGet(schemaUrl);
            String content = new String(bytes);
            Document document = DocumentHelper.parseText(content);
            root = document.getRootElement();
        } catch (Exception e) {
            throw new WsdlParserException(e);
        }
        parseSchema(root, messages);
        return true;
    }

    /**
     * 解析import链接里的schema
     */
    @SuppressWarnings("unchecked")
    private static void parseSchema(Element schema, List<Message> messages) throws WsdlParserException {
        List<Element> elements = schema.elements(SCHEMA_ELEMENT_ELEMENT);
        for (Message m : messages) {
            Element findEle = findSafeEleByAttrEquals(elements, SCHEMA_ELEMENT_NAME_ATTR, m.element);
            List<Element> es;

            String type = getUnSafeAttributeValue(findEle, SCHEMA_ELEMENT_TYPE_ATTR);
            if (!StringUtils.isEmpty(type)) {
                List<Element> complexes = schema.elements(COMPLEX_TYPE_ELEMENT);
                Element findComplex = findSafeEleByAttrEquals(complexes, COMPLEX_NAME_ATTR, type);
                es = getSafeChildElement(findComplex, SEQUENCE_ELEMENT).elements(SEQUENCE_ELEMENT_ELEMENT);
            } else {
                es = getSafeChildElement(findEle, COMPLEX_TYPE_ELEMENT, SEQUENCE_ELEMENT)
                        .elements(SEQUENCE_ELEMENT_ELEMENT);
            }
            for (Element e : es) {
                MessageElement messageElement = new MessageElement();
                messageElement.name = getSafeShortAttributeValue(e, NAME_ATTR);
                messageElement.type = getSafeShortAttributeValue(e, TYPE_ATTR);
                m.elements.add(messageElement);
            }
        }
    }


    /**
     * 解析版本号
     */
    private static void parseVersion(Element definitions, WsdlResult result) throws WsdlParserException {
        Element address = getSafeChildElement(definitions, SERVICE_ELEMENT, PORT_ELEMENT, ADDRESS_ELEMENT);
        String prefix = getSafeElementNamespacePrefix(address);
        result.version = prefix.endsWith("12") ? SoapMessage.Version.V1_2 : SoapMessage.Version.V1_1;
    }


    /**
     * element操作
     */
    private static Element getSafeChildElement(Element parent, String... keys) throws WsdlParserException {
        Element cur = parent;
        for (int index = 0, len = keys.length; index < len; index++) {
            cur = cur.element(keys[index]);
            if (cur == null) {
                if (index == 0) {
                    throw new WsdlParserException("can not get '" + keys[index] + "' element from '" + parent.getName() + "'");
                }
                throw new WsdlParserException("can not get '" + keys[index] + "' element from '" + keys[index - 1] + "'");
            }
        }
        return cur;
    }

    private static String getSafeElementNamespacePrefix(Element element) throws WsdlParserException {
        String prefix = element.getNamespacePrefix();
        if (prefix == null) {
            throw new WsdlParserException("can not get namespace prefix from '" + element.getName() + "' element");
        }
        return prefix;
    }


    private static Attribute getSafeAttribute(Element element, String attr) throws WsdlParserException {
        Attribute attribute = element.attribute(attr);
        if (attribute == null) {
            throw new WsdlParserException("can not get '" + attr + "' attr from '" + element.getName() + "'");
        }
        return attribute;
    }

    private static String getSafeAttributeValue(Element element, String attr) throws WsdlParserException {
        String value = getSafeAttribute(element, attr).getValue();
        if (value == null) {
            throw new WsdlParserException("get '" + attr + "' attr from '" + element.getName() + "' is null");
        }
        return value;
    }

    private static String getSafeShortAttributeValue(Element element, String attr) throws WsdlParserException {
        String value = getSafeAttributeValue(element,attr);
        return value.contains(":") ? value.split(":")[1] : value;
    }

    private static String getUnSafeAttributeValue(Element element, String attr) {
        Attribute attribute = element.attribute(attr);
        if (attribute == null) {
            return null;
        }
        String v = attribute.getValue();
        if (v == null) {
            return null;
        }
        return v.contains(":") ? v.split(":")[1] : v;
    }

    private static Element findSafeEleByAttrEquals(List<Element> elements, String name, String value) throws WsdlParserException {
        Element find = null;
        for (Element e : elements) {
            Attribute attr = e.attribute(name);
            if (attr != null && value.equals(attr.getValue())) {
                find = e;
                break;
            }
        }
        if (find == null) {
            throw new WsdlParserException("can not find element where attr='" + name + "', value='" + value + "'");
        }
        return find;
    }


    static class Service {
        //element
        static final String SERVICE_ELEMENT = "service";
        static final String PORT_ELEMENT = "port";
        static final String ADDRESS_ELEMENT = "address";

        //attr
        static final String SERVICE_NAME_ATTR = "name";
        static final String PORT_NAME_ATTR = "name";
        static final String PORT_BINDING_ATTR = "binding";
        static final String LOCATION_ATTR = "location";

        String name;
        String portName;
        String portBinding;
        String address;
        Binding binding;
    }

    static class Binding {
        //element
        static final String BINDING_ELEMENT = "binding";
        static final String SUB_BINDING_ELEMENT = "binding";

        //attr
        static final String BINDING_NAME_ATTR = "name";
        static final String BINDING_TYPE_ATTR = "type";
        static final String TRANSPORT_ATTR = "transport";
        static final String STYLE_ATTR = "style";

        String name;
        String type;
        String style;
        String transport;
        PortType portType;
    }

    static class PortType {
        //element
        static final String PORT_TYPE_ELEMENT = "portType";
        //attr
        static final String PORT_TYPE_NAME_ATTR = "name";

        String name;
        List<Operation> operations;
    }

    static class Operation {
        //element
        static final String OPERATION_ELEMENT = "operation";
        static final String INPUT_ELEMENT = "input";
        static final String OUTPUT_ELEMENT = "output";

        //attr
        static final String OPERATION_NAME_ATTR = "name";
        static final String MESSAGE_ATTR = "message";

        String name;
        Message inputMessage;
        Message outputMessage;

    }

    static class Message {
        //element
        static final String MESSAGE_ELEMENT = "message";
        static final String MESSAGE_PART_ELEMENT = "part";

        //attr
        static final String PART_ELEMENT_ARRT = "element";
        static final String MESSAGE_NAME_ATTR = "name";

        String name;
        String use;
        String element;
        List<MessageElement> elements = new ArrayList<>();
    }


    static class MessageElement {

        static final String NAME_ATTR = "name";
        static final String TYPE_ATTR = "type";

        String name;
        String type;

    }

    static class Types {

        static final String TYPES_ELEMENT = "types";
        static final String SCHEMA_ELEMENT = "schema";
        static final String SCHEMA_ELEMENT_ELEMENT = "element";
        static final String IMPORT_ELEMENT = "import";
        static final String COMPLEX_TYPE_ELEMENT = "complexType";
        static final String SEQUENCE_ELEMENT = "sequence";
        static final String SEQUENCE_ELEMENT_ELEMENT = "element";

        static final String SCHEMA_ELEMENT_NAME_ATTR = "name";
        static final String SCHEMA_ELEMENT_TYPE_ATTR = "type";
        static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
        static final String COMPLEX_NAME_ATTR = "name";

    }

}




