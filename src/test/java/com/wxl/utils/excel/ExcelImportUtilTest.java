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
public class ExcelImportUtilTest {

    @Test
    public void imports() throws Exception {
        FileInputStream in = new FileInputStream("src/test/resources/map.xlsx");
        List<User> list = ExcelImportUtil.imports(in,User.class,1);
        System.out.println("导入完成");
        for(User user:list){
            System.out.println(user);
        }
    }

    @Test
    public void importAll() throws Exception{
        FileInputStream in = new FileInputStream("src/test/resources/map.xlsx");
        Workbook workbook = ExcelImportUtil.createTmp(in);
        List<User> list1 = ExcelImportUtil.importFromTmp(workbook,User.class,0,1);
        List<List<String>> list2 = ExcelImportUtil.importFromTmp(workbook,1,1);
        List<List<String>> list3 = ExcelImportUtil.importFromTmp(workbook,2,1);
        ExcelImportUtil.destroyTmp(workbook);
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




