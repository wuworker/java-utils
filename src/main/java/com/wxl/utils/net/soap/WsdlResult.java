package com.wxl.utils.net.soap;

import com.wxl.utils.annotation.ThreadSafe;

import java.util.*;

/**
 * Created by wuxingle on 2018/1/11.
 * wsdl解析结果
 */
@ThreadSafe
public class WsdlResult {

    String targetNamespace;

    SoapMessage.Version version;

    WsdlParser.Service service;

    WsdlParser.Binding binding;

    WsdlParser.PortType portType;

    List<WsdlParser.Operation> operations;

    List<WsdlParser.Message> messages;

    private List<String> operationList;

    private Map<String, List<String>> requestParams;

    private Map<String, String> responses;

    /**
     * targetNamespace
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * 版本号
     */
    public SoapMessage.Version getVersion() {
        return version;
    }

    /**
     * 服务名
     */
    public String getServiceName() {
        return service.name;
    }

    /**
     * 服务地址
     */
    public String getServiceAddress() {
        return service.address;
    }

    /**
     * 端口
     */
    public String getPortType() {
        return portType.name;
    }

    /**
     * 获取所有操作
     */
    public List<String> getOperations() {
        if (operationList == null) {
            synchronized (this) {
                if (operationList == null) {
                    List<String> list = new ArrayList<>(operations.size());
                    for (WsdlParser.Operation op : operations) {
                        list.add(op.name);
                    }
                    operationList = Collections.unmodifiableList(list);
                }
            }
        }
        return operationList;
    }

    /**
     * 获取请求参数名
     *
     * @param operation 操作
     */
    public List<String> getRequestParams(String operation) {
        if (requestParams == null) {
            synchronized (this) {
                if (requestParams == null) {
                    requestParams = new HashMap<>();
                    for (WsdlParser.Operation op : operations) {
                        WsdlParser.Message input = op.inputMessage;
                        if (input != null) {
                            List<WsdlParser.MessageElement> elements = input.elements;
                            List<String> params = new ArrayList<>(elements.size());
                            for (WsdlParser.MessageElement e : elements) {
                                params.add(e.name);
                            }
                            requestParams.put(op.name, params);
                        }
                    }
                }
            }
        }
        return requestParams.get(operation);
    }

    /**
     * 获取响应名
     *
     * @param operation 操作
     */
    public String getResponse(String operation) {
        if (responses == null) {
            synchronized (this) {
                if (responses == null) {
                    responses = new HashMap<>(operations.size(), 1);
                    for (WsdlParser.Operation op : operations) {
                        WsdlParser.Message output = op.outputMessage;
                        if (output != null) {
                            responses.put(operation, output.name);
                        }
                    }
                }
            }
        }
        return responses.get(operation);
    }

    /**
     * 根据操作创建请求
     *
     * @param operation 操作
     * @param values    请求值
     * @return soap请求消息
     */
    public SoapMessage buildSoapRequest(String operation, String... values) {
        String prefix, portType, serviceName;
        if ((portType = getPortType()).length() > 3) {
            prefix = portType.substring(0, 3).toLowerCase();
        } else if ((serviceName = getServiceName()).length() > 3) {
            prefix = serviceName.substring(0, 3).toLowerCase();
        } else {
            prefix = "wxl";
        }
        SoapMessageBuilder builder = SoapMessage.create(version)
                .addBody(operation, prefix, targetNamespace);
        List<String> requestParams = getRequestParams(operation);
        int i;
        for (i = 0; i < requestParams.size() && i < values.length; i++) {
            builder.addBodyValue(operation, requestParams.get(i), values[i]);
        }
        for (; i < requestParams.size(); i++) {
            builder.addBodyValue(operation, requestParams.get(i), "");
        }
        return builder.build();
    }


}










