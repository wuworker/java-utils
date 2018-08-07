package com.wxl.utils.base.serializable;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by wuxingle on 2017/12/2 0002.
 */
public class UserC implements Serializable{


    private static final long serialVersionUID = -8838175761064287541L;

    private String name;

    private void readObjectNoData() throws ObjectStreamException {
        System.out.println("版本不一致，输入流被篡改等异常出现时自动调用");
        name="default";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserC{" +
                "name='" + name + '\'' +
                '}';
    }
}
