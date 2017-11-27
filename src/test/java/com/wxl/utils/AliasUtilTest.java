package com.wxl.utils;

import com.wxl.utils.AliasUtil;
import org.junit.Test;

/**
 * Created by wuxingle on 2017/11/24.
 *
 */
public class AliasUtilTest {

    @Test
    public void snakeToCamel() {
        String s1 = AliasUtil.snakeToCamel("user_name");
        String s2 = AliasUtil.snakeToCamel("_abc");
        String s3 = AliasUtil.snakeToCamel("password__");
        String s4 = AliasUtil.snakeToCamel("app_user_mobile_phone");
        String s5 = AliasUtil.snakeToCamel("_app_user__mobile_phone_");
        String s6 = AliasUtil.snakeToCamel("abc___abc_");

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
        System.out.println(s5);
        System.out.println(s6);
    }

    @Test
    public void camelToSnake(){
        System.out.println(AliasUtil.camelToSnake("userName"));
        System.out.println(AliasUtil.camelToSnake("Abc"));
        System.out.println(AliasUtil.camelToSnake("password"));
        System.out.println(AliasUtil.camelToSnake("appUserMobilePhone"));
        System.out.println(AliasUtil.camelToSnake("AppUserMobilePhone"));
        System.out.println(AliasUtil.camelToSnake("ABC"));
    }
}






