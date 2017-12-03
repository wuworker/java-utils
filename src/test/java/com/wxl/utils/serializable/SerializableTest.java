package com.wxl.utils.serializable;

import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by wuxingle on 2017/12/2 0002.
 * 序列化测试
 */
public class SerializableTest {

    private static final String PATH = "src/test/resources/com/wxl/utils/serializable/";

    @Test
    public void testWrite()throws Exception{
        UserA a = new UserA("哈哈",23);
       // a.setUserB(new UserB("B",11));
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(getFilePath("usera.txt")))){
            out.writeObject(a);
        }
    }

    @Test
    public void testRead()throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(getFilePath("usera.txt")))){
            Object obj = in.readObject();
            System.out.println(obj.getClass());
            System.out.println(obj);
        }
    }

    @Test
    public void testWR()throws Exception{
        UserA a = new UserA("哈哈",23);
       // a.setUserB(new UserB("B",11));
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(getFilePath("usera.txt")));
             ObjectInputStream in = new ObjectInputStream(
                     new FileInputStream(getFilePath("usera.txt")))){
            out.writeObject(a);
            Object obj = in.readObject();

            System.out.println(obj);
            System.out.println(obj == a);
        }
    }


    @Test
    public void testDependence(){
        Address address = new Address("aa");
        User user = new User(address);

        System.out.println(user);
        System.out.println(user.getAddress());
        System.out.println(user.getBook());
        System.out.println(user.getBook().getAddress());

        byte[] bytes = SerializationUtils.serialize(user);
        User user2 = (User)SerializationUtils.deserialize(bytes);

        System.out.println();
        System.out.println(user2);
        System.out.println(user2.getAddress());
        System.out.println(user2.getBook());
        System.out.println(user2.getBook().getAddress());


    }





    private String getFilePath(String name){
        return PATH + name;
    }

}