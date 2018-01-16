package com.wxl.utils.net.soap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.Assert;

/**
 * Created by wuxingle on 2018/1/10.
 * soap消息
 */
public class SoapMessage {

    //命名空间
    public static final String NAMESPACE_URI_11 = "http://schemas.xmlsoap.org/soap/envelope/";

    public static final String NAMESPACE_URI_12 = "http://www.w3.org/2003/05/soap-envelope";

    public static final String ENCODING_STYLE_URI_11 = "http://schemas.xmlsoap.org/soap/encoding/";

    public static final String ENCODING_STYLE_URI_12 = "http://www.w3.org/2001/12/soap-encoding";

    //element
    static final String ENVELOPE_ELEMENT = "Envelope";

    static final String HEAD_ELEMENT = "Header";

    static final String BODY_ELEMENT = "Body";

    static final String FAULT_ELEMENT = "Fault";

    static final String FAULT_CODE_ELEMENT = "faultcode";

    static final String FAULT_STRING_ELEMENT = "faultstring";

    static final String FAULT_ACTOR_ELEMENT = "faultactor";

    static final String FAULT_DETAIL_ELEMENT = "detail";


    //attribute
    static final String ENCODING_STYLE_ATTRIBUTE = "encodingStyle";

    static final String ACTOR_HEADER_ATTRIBUTE = "actor";

    static final String MUST_UNDERSTAND_HEADER_ATTRIBUTE = "mustUnderstand";


    //版本号
    public enum Version {
        V1_1,
        V1_2
    }


    /**
     * 获取构建类
     */
    public static SoapMessageBuilder create(Version version) {
        return new SoapMessageBuilder(version);
    }

    public static SoapMessageBuilder create(String prefix, Version version) {
        return new SoapMessageBuilder(prefix, version);
    }

    /**
     * 根据uri获取版本
     */
    public static Version getVersionByNamespaceURI(String uri) {
        if (NAMESPACE_URI_11.equals(uri)) {
            return Version.V1_1;
        } else if (NAMESPACE_URI_12.equals(uri)) {
            return Version.V1_2;
        }
        return null;
    }

    /**
     * 根据版本获取uri
     */
    public static String getNamespaceURIByVersion(Version version) {
        Assert.notNull(version, "version can not null");
        switch (version) {
            case V1_1:
                return NAMESPACE_URI_11;
            case V1_2:
                return NAMESPACE_URI_12;
            default:
                return null;
        }
    }

    /**
     * 解析成soap消息
     */
    public static SoapMessage parse(String soapText) throws SoapException {
        Document document;
        try {
            document = DocumentHelper.parseText(soapText);
        } catch (DocumentException e) {
            throw new SoapException(e);
        }
        Element root = document.getRootElement();
        if(root == null){
            throw new SoapException("soap element 'Envelope' can not absent!");
        }
        if (!root.getName().equals(ENVELOPE_ELEMENT)) {
            throw new SoapException("is not a soap message,root must is 'Envelope'!");
        }
        String namespaceUri = root.getNamespaceURI();
        String prefix = root.getNamespacePrefix();
        Version version = getVersionByNamespaceURI(namespaceUri);
        if(version == null){
            throw new SoapException("unknow NamespaceUri: " + namespaceUri);
        }
        SoapMessage soapMessage = new SoapMessage();
        soapMessage.version = version;
        soapMessage.prefix = prefix;
        soapMessage.namespaceUri = namespaceUri;
        soapMessage.envelope = root;
        soapMessage.header = root.element(HEAD_ELEMENT);
        soapMessage.body = root.element(BODY_ELEMENT);
        if(soapMessage.body == null){
            throw new SoapException("soap element 'Body' can not absent!");
        }
        soapMessage.fault = soapMessage.body.element(FAULT_ELEMENT);

        return soapMessage;
    }


    private String prefix;

    private String namespaceUri;

    private Element envelope;

    private Element header;

    private Element body;

    private Element fault;

    private Version version;

    private SoapMessage() {}

    SoapMessage(String prefix, String namespaceUri, Element envelope,
                Element header, Element body, Element fault, Version version) {
        this.prefix = prefix;
        this.namespaceUri = namespaceUri;
        this.envelope = envelope;
        this.header = header;
        this.body = body;
        this.fault = fault;
        this.version = version;
    }

    public String getPrefix(){
        return prefix;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public Version getVersion(){
        return version;
    }

    /**
     * 获取响应
     */
    public String getResponse(){
        return body.getStringValue();
    }

    /**
     * 获取错误信息
     */
    public String getFaultCode() {
        return getFaultMessage(FAULT_CODE_ELEMENT);
    }

    public String getFaultString() {
        return getFaultMessage(FAULT_STRING_ELEMENT);
    }

    public String getFaultActor() {
        return getFaultMessage(FAULT_ACTOR_ELEMENT);
    }

    public String getFaultDetail() {
        return getFaultMessage(FAULT_DETAIL_ELEMENT);
    }

    private String getFaultMessage(String faultElement) {
        if (fault == null) {
            return null;
        }
        Element element = fault.element(faultElement);
        if (element == null) {
            return null;
        }
        return element.getText();
    }

    /**
     * 消息是否成功
     */
    public boolean success() {
        return fault == null;
    }

    /**
     * 获取element
     */
    public Element getHeader() {
        return header;
    }

    public Element getBody() {
        return body;
    }


    @Override
    public String toString() {
        return envelope.asXML();
    }

}



