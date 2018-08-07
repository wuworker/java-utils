package com.wxl.utils.base.cloneable;

import org.junit.Test;

/**
 * Created by wuxingle on 2017/12/2 0002.
 *
 */
public class CloneableTest {

    @Test
    public void testClone(){
        UserB b = new UserB("拉拉");
        UserA a = new UserA("哈哈",12,b);


        System.out.println(a);
        System.out.println(a.getUserB());

        UserA a2 = a.clone();
        System.out.println(a2);
        System.out.println(a2.getUserB());
    }







}
