package com.wxl.utils.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * excel读取
 */
public class ExcelImportUtilsTest {

    @Test
    public void imports() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/map.xlsx");
        List<User> list = ExcelImportUtils.imports(in,User.class,1);
        System.out.println("导入完成");
        for(User user:list){
            System.out.println(user);
        }
    }

    @Test
    public void importAll() throws Exception{
        FileInputStream in = new FileInputStream("src/test/resources/map.xlsx");
        Workbook workbook = ExcelImportUtils.createTmp(in);
        List<User> list1 = ExcelImportUtils.importFromTmp(workbook,User.class,0,1);
        List<List<String>> list2 = ExcelImportUtils.importFromTmp(workbook,1,1);
        List<List<String>> list3 = ExcelImportUtils.importFromTmp(workbook,2,1);
        ExcelImportUtils.closeInput(in);
        System.out.println(list1);
        System.out.println(list2);
        System.out.println(list3);
    }

    @Test
    public void testDuble(){
        double num = 2.084188425E9;
        BigDecimal bigDecimal = new BigDecimal(num);
        System.out.println(bigDecimal);
        System.out.println(num);
    }
}




