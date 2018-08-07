package com.wxl.utils.http.soap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.junit.Test;

/**
 * Created by wuxingle on 2018/1/10.
 * soap消息测试
 */
public class SoapMessageTest {


    @Test
    public void test1(){
        SoapMessageBuilder soapMessageBuilder = new SoapMessageBuilder(SoapMessage.Version.V1_1);

        soapMessageBuilder.addBody("sayHello","hel","http://www.baidu.com/")
                .addBodyValue("sayHello","arg0","abc")
                .addBodyValue("sayHello","arg1","12");


        soapMessageBuilder.setEncodingStyle(soapMessageBuilder.getBody(),SoapMessage.ENCODING_STYLE_URI_11)
                .setEncodingStyle(soapMessageBuilder.getEnvelope(),SoapMessage.ENCODING_STYLE_URI_11)
                .addHeader("nice","yes")
                .addMustUnderstand("nice",true)
                .addActor("nice","htt");

        Element element = DocumentHelper.createElement(new QName(
                "Trans",new Namespace("m","http://www.w3schools.com/transaction/")));
        element.addText("123");
        soapMessageBuilder.addHeader(element);

        SoapMessage soapMessage = soapMessageBuilder.build();

        System.out.println(soapMessage);
    }

    @Test
    public void test2()throws SoapException{
        String text="<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <S:Body>\n" +
                "      <ns2:sayHello1Response xmlns:ns2=\"http://service.wxl.com/HelloWorld\">\n" +
                "         <return>lalla,Hello World111111,999</return>\n" +
                "      </ns2:sayHello1Response>\n" +
                "   </S:Body>\n" +
                "</S:Envelope>";

        SoapMessage soapMessage = SoapMessage.parse(text);
        System.out.println(soapMessage);
        System.out.println(soapMessage.success());
        System.out.println(soapMessage.getFaultCode());
        System.out.println(soapMessage.getFaultString());
        System.out.println(soapMessage.getResponse());
    }

    @Test
    public void test3(){
        SoapMessage soapMessage = SoapMessage.create("wxl",SoapMessage.Version.V1_2)
                .addHeader("hehe","123")
                .addMustUnderstand("hehe",false)
                .addActor("hehe","http://www.biadu.com/")
                .addBody("sayHello1","hel1","http://www.baidu.com1/")
                .addBodyValue("sayHello1","arg01","回火1")
                .addBodyValue("sayHello1","arg11","121")
                .build();

        System.out.println(soapMessage);
    }

}












