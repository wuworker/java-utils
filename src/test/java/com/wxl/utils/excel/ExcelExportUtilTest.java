package com.wxl.utils.excel;

import com.wxl.utils.excel.prop.ExcelVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by wuxingle on 2017/10/21 0021.
 *
 */
public class ExcelExportUtilTest {

    @Test
    public void export() throws Exception {
        List<User> list = new ArrayList<>();
        List<Map<String,Object>> maps = new ArrayList<>();
        List<Integer> strings = new ArrayList<>();
        Random random = new Random();
        for(int i=0;i<10;i++){
            if(i == 5){
                list.add(null);
                maps.add(null);
                strings.add(null);
            }
            list.add(createUser(random));
            maps.add(createMap(random));
            strings.add(random.nextInt());
        }

        OutputStream out = new FileOutputStream("src/test/resources/map.xlsx");
        Workbook workbook = ExcelExportUtil.createTmp(ExcelVersion.XLSX);
        ExcelExportUtil.exportToTmp(workbook,list,"用户",new String[]{"id","姓名","年龄","手机","地址"});
        ExcelExportUtil.exportToTmp(workbook,maps,"map",new String[]{"姓名","年龄","手机","地址"});
        ExcelExportUtil.exportToTmp(workbook,strings,"strings",new String[]{"字符串"});
        ExcelExportUtil.outputTmpAndClose(workbook,out);
    }


    @Test
    public void testExport2()throws IOException{
        Random random = new Random();
        List<Map<String,Object>> list = new ArrayList<>();
        for(int i=0;i<20;i++){
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("序号",(i+1));
            map.put("选项名称",random.nextInt(100));
            map.put("选项图片","http://aicixi.img-cn-shanghai.aliyuncs.com/20170901081437610_44.jpg");
            map.put("投票次数",random.nextInt());
            map.put("参与用户ip","用户"+random.nextInt());
            list.add(map);
        }
        OutputStream out = new FileOutputStream("src/test/resources/map2.xlsx");

        ExcelExportUtil.export(list,out,ExcelVersion.XLSX);
    }




    private User createUser(Random random){
        User user = new User();
        user.setId(random.nextInt());
        user.setName(""+(char)(random.nextInt(26)+'a')+(char)(random.nextInt(26)+'A'));
        user.setAge(random.nextInt(100));
        user.setMobilePhone((random.nextInt()+10000000000L)+"");
        user.setAddress("浙江"+random.nextInt()+"街道");
        return user;
    }

    private Map<String,Object> createMap(Random random){
        Map<String,Object> map = new LinkedHashMap<>();
        map.put("姓名",""+(char)(random.nextInt(26)+'a')+(char)(random.nextInt(26)+'A'));
        map.put("年龄",random.nextInt(100));
        map.put("手机",(random.nextInt()+10000000000L)+"");
        map.put("地址","浙江"+random.nextInt()+"街道");
        return map;
    }

}