package com.wxl.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by wuxingle on 2017/12/17 0017.
 * 集合工具类
 */
public class ListMapUtilsTest {

    @Test
    public void testPageOfList(){
        Random random = new Random(1);
        List<String> list = RandomUtils.generateObject(random,10,
                (r)->RandomUtils.generateAbcNum(r,5));
        System.out.println(list);

        List<String> list1 = ListMapUtils.pageOfList(list, 1, 3);
        System.out.println(list1);

        List<String> list2 = ListMapUtils.pageOfList(list, 2, 5);
        System.out.println(list2);

        List<String> list3 = ListMapUtils.pageOfList(list, 3, 4);
        System.out.println(list3);
    }


    @Test
    public void testGetMapValue(){
        Map<String,Object> map = new HashMap<>();
        map.put("1",2);
        map.put("2","234");

        Integer mapValue = ListMapUtils.getMapValue(map, "1", Integer.class);
        System.out.println(mapValue);
        String mapValue1 = ListMapUtils.getMapValue(map, "3", String.class);
        System.out.println(mapValue1);
    }


}


