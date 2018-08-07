package com.wxl.utils.base;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        String s7 = AliasUtils.snakeToCamel("abcDFs");

        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s4);
        System.out.println(s5);
        System.out.println(s6);
        System.out.println(s7);
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


    @Test
    public void testAliasJsonKey(){
        Map<String,Object> json = new HashMap<>();
        JsonUtils.put(json,"user.info.real_name","小明");
        JsonUtils.put(json,"user.info.mobile_phone","13456789098");
        JsonUtils.put(json,"user.info.address","中国");
        JsonUtils.put(json,"user.grade.yu_wen","85");
        JsonUtils.put(json,"user.grade.shu_xue","99");
        JsonUtils.put(json,"user.grade.li_zong.0","97");
        JsonUtils.put(json,"user.grade.li_zong.1","98");
        JsonUtils.put(json,"user.grade.li_zong.2","95");
        JsonUtils.put(json,"access_token","abcdefg");
        JsonUtils.put(json,"refresh_token","poiuytre");
        JsonUtils.put(json,"access_token_expire",1000);
        JsonUtils.put(json,"refresh_token_expire",3600);

        System.out.println(JsonUtils.toPrettyFormat(json));

        AliasUtils.aliasJsonKey(json,AliasUtils::snakeToCamel);

        System.out.println(JsonUtils.toPrettyFormat(json));
    }




}






