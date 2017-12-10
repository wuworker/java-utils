package com.wxl.utils;

import org.junit.Test;

/**
 * Created by wuxingle on 2017/11/24.
 *
 */
public class AliasUtilsTest {

    @Test
    public void snakeToCamel() {
        String s1 = AliasUtils.snakeToCamel("user_name");
        String s2 = AliasUtils.snakeToCamel("_abc");
        String s3 = AliasUtils.snakeToCamel("password__");
        String s4 = AliasUtils.snakeToCamel("app_user_mobile_phone");
        String s5 = AliasUtils.snakeToCamel("_app_user__mobile_phone_");
        String s6 = AliasUtils.snakeToCamel("abc___abc_");

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
        System.out.println(s5);
        System.out.println(s6);
    }

    @Test
    public void camelToSnake(){
        System.out.println(AliasUtils.camelToSnake("userName"));
        System.out.println(AliasUtils.camelToSnake("Abc"));
        System.out.println(AliasUtils.camelToSnake("password"));
        System.out.println(AliasUtils.camelToSnake("appUserMobilePhone"));
        System.out.println(AliasUtils.camelToSnake("AppUserMobilePhone"));
        System.out.println(AliasUtils.camelToSnake("ABC"));
    }
}






