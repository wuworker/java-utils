package com.wxl.utils.http.soap;

import com.wxl.utils.base.Dom4jUtils;
import org.junit.Test;

/**
 * Created by wuxingle on 2018/1/12.
 * WsdlParserTest
 */
public class WsdlParserTest {

    @Test
    public void test1() throws WsdlParserException {
        WsdlResult wsdlResult = WsdlParser.parseFromURL("http://localhost:9000/HelloWorld?wsdl");
        System.out.println(wsdlResult.getTargetNamespace());
        System.out.println(wsdlResult.getPortType());
        System.out.println(wsdlResult.getServiceName());
        System.out.println(wsdlResult.getServiceAddress());
        System.out.println(wsdlResult.getVersion());
        System.out.println(wsdlResult.getOperations());
        for(String key : wsdlResult.getOperations()){
            System.out.println("key:"+key+"--->"+wsdlResult.getRequestParams(key));
        }

        SoapMessage soapMessage = wsdlResult.buildSoapRequest("sayHello","哈哈");
        System.out.println(Dom4jUtils.toPrettyFormat(soapMessage.toString()));
    }















}