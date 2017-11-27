package com.wxl.utils.excel;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * 用户类
 */
@Data
public class User {

    private Integer id;

    private String name;

    private Integer age;

    private String mobilePhone;

    private String address;

    private String birthday;

    public String getBirthday() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(System.currentTimeMillis() - new Random().nextInt()));
    }

}
