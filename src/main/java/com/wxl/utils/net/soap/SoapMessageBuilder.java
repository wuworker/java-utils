package com.wxl.utils.net.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.springframework.util.Assert;

import static com.wxl.utils.net.soap.SoapMessage.*;

/**
 * Created by wuxingle on 2018/1/10.
 * 通过这个类创建soap消息
 */
public class SoapMessageBuilder {

    private String prefix;

    private Namespace ns;

    private Element envelope;

    private Element header;

    private Element body;

    private Element fault;

    private Version version;


    public SoapMessageBuilder(Version version) {
        this("soap", version);
    }

    public SoapMessageBuilder(String prefix, Version version) {
        Assert.notNull(version, "version can not null");
        this.prefix = prefix;
        this.version = version;

        ns = new Namespace(prefix, getNamespaceURIByVersion(version));

        envelope = DocumentHelper.createElement(new QName(ENVELOPE_ELEMENT, ns));
        body = DocumentHelper.createElement(new QName(BODY_ELEMENT, ns));

        envelope.add(body);
    }

    public SoapMessage build() {
        return new SoapMessage(prefix, ns.getURI(), envelope, header, body, fault, version);
    }


    /**
     * 设置encodingStyle
     */
    public SoapMessageBuilder setEncodingStyle(Element element, String uri) {
        element.addAttribute(new QName(ENCODING_STYLE_ATTRIBUTE, ns), uri);
        return this;
    }

    /**
     * 设置header
     */
    public SoapMessageBuilder addHeader(String name, String value) {
        Element element = DocumentHelper.createElement(name);
        element.setText(value);
        getHeader().add(element);
        return this;
    }

    public SoapMessageBuilder addHeader(Element element) {
        getHeader().add(element);
        return this;
    }

    public SoapMessageBuilder addMustUnderstand(String elementName, boolean mustUnderstand) {
        Element element = getHeader().element(elementName);
        if (element == null) {
            throw new IllegalArgumentException("can not found element '" + elementName + "' in 'Header'");
        }
        element.addAttribute(new QName(MUST_UNDERSTAND_HEADER_ATTRIBUTE, ns), mustUnderstand ? "1" : "0");
        return this;
    }

    public SoapMessageBuilder addActor(String elementName, String uri) {
        Element element = getHeader().element(elementName);
        if (element == null) {
            throw new IllegalArgumentException("can not found element '" + elementName + "' in 'Header'");
        }
        element.addAttribute(new QName(ACTOR_HEADER_ATTRIBUTE, ns), uri);
        return this;
    }


    /**
     * 设置body
     */
    public SoapMessageBuilder addBody(String name, String prefix, String uri) {
        Namespace namespace = this.prefix.equals(prefix) ? ns : new Namespace(prefix, uri);
        body.add(DocumentHelper.createElement(new QName(name, namespace)));
        return this;
    }

    public SoapMessageBuilder addBody(Element element) {
        body.add(element);
        return this;
    }

    public SoapMessageBuilder addBodyValue(String bodyName, String key, String value) {
        Element element = body.element(bodyName);
        if (element == null) {
            throw new IllegalArgumentException("can not found element '" + bodyName + "' in 'Body'");
        }
        Element keyEle = DocumentHelper.createElement(key);
        keyEle.setText(value);
        element.add(keyEle);
        return this;
    }


    /**
     * 设置fault
     */
    public SoapMessageBuilder setFaultCode(String faultcode) {
        setFaultMessage(FAULT_CODE_ELEMENT, faultcode);
        return this;
    }

    public SoapMessageBuilder setFaultString(String faultstring) {
        setFaultMessage(FAULT_STRING_ELEMENT, faultstring);
        return this;
    }

    public SoapMessageBuilder setFaultActor(String faultcode) {
        setFaultMessage(FAULT_ACTOR_ELEMENT, faultcode);
        return this;
    }

    public SoapMessageBuilder setFaultDetail(String faultcode) {
        setFaultMessage(FAULT_DETAIL_ELEMENT, faultcode);
        return this;
    }

    @SuppressWarnings("unchecked")
    private void setFaultMessage(String faultElement, String msg) {
        if (fault == null) {
            fault = DocumentHelper.createElement(new QName(FAULT_ELEMENT, ns));
            body.content().add(0,fault);
        }
        Element element = fault.element(faultElement);
        if (element == null) {
            element = DocumentHelper.createElement(faultElement);
        }
        element.setText(msg);
        fault.add(element);
    }

    public Element getEnvelope() {
        return envelope;
    }

    public Element getBody() {
        return body;
    }

    @SuppressWarnings("unchecked")
    public Element getHeader() {
        if (header == null) {
            header = DocumentHelper.createElement(new QName(HEAD_ELEMENT, ns));
            envelope.content().add(0, header);
        }
        return header;
    }

}

