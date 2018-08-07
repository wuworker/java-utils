package com.wxl.utils.file.excel;

import com.wxl.utils.file.TestHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Created by wuxingle on 2017/10/21 0021.
 * excel读取
 */
public class ExcelImportUtilsTest {

    @Test
    public void imports() throws Exception {
        InputStream in = TestHelper.getFileInputStream(ExcelImportUtilsTest.class, "map.xlsx");

        List<User> list = ExcelImportUtils.imports(in, User.class, 1);
        System.out.println("导入完成");
        for (User user : list) {
            System.out.println(user);
        }
    }

    @Test
    public void imports2() throws Exception {
        InputStream in = TestHelper.getFileInputStream(ExcelImportUtilsTest.class, "map1.xlsx");
        List<List<String>> imports = ExcelImportUtils.imports(in, 1);
        for (List<String> list : imports) {
            System.out.println(list);
        }
    }

    @Test
    public void importAll() throws Exception {
        InputStream in = TestHelper.getFileInputStream(ExcelImportUtilsTest.class, "map.xlsx");
        Workbook workbook = ExcelImportUtils.createTmp(in);
        List<User> list1 = ExcelImportUtils.importFromTmp(workbook, User.class, 0, 1);
        List<List<String>> list2 = ExcelImportUtils.importFromTmp(workbook, 1, 1);
        List<List<String>> list3 = ExcelImportUtils.importFromTmp(workbook, 2, 1);
        ExcelImportUtils.closeInput(in);
        System.out.println(list1);
        System.out.println(list2);
        System.out.println(list3);
    }


}




