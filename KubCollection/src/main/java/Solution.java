//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

import com.csvreader.CsvWriter;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class Solution {

    public static void main(String[] args) throws Exception {

        //输入时间 例如：2018-05-06 02:03:00.000
        Scanner input = new Scanner(System.in);
        String inputStartTime = input.nextLine();
        String inputEndTime = input.nextLine();

        //输入文件名，可在源码处修改文件路径
        String fileName = input.nextLine();
        String directoryPath = "/Users/zhangziyang/Desktop/csvexercise/out/";
        String csv = ".csv";
        String filePath = directoryPath + fileName + csv ;

        //定义起止时间变量
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date1 = sdf.parse(inputStartTime);
        Date date2 = sdf.parse(inputEndTime);

        //定义格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        //将北京时间转换成格林威治时间
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        String startTime = simpleDateFormat.format(date1);

        String endTime = simpleDateFormat.format(date2);


        //MemoryUsage样例
        String nodeUrl = "http://10.60.38.181:10001/api/v1/model/nodes/rain-u2/metrics/memory/usage?start="+startTime+"&end="+endTime;

        String response = HttpRequest.get(nodeUrl).body();

        //去掉json开头的无用数据，只留下【 】之内的数据
        String resJson = stringCut("[","]",response);

        JSONArray jsonArray = JSONArray.fromObject(resJson);

        //为timestamp、memoryusage实例化对象
        ArrayList<NodeData> nodeDatas = new ArrayList<NodeData>();
        for (int i = 0;i < jsonArray.size();i++){
            NodeData nodeData = new NodeData();
            nodeData.setTimeStamp(jsonArray.getJSONObject(i).getString("timestamp"));
            nodeData.setMemoryUsage(jsonArray.getJSONObject(i).getString("value"));
            nodeDatas.add(nodeData);

        }

       //除Memory外遍历时，访问的Url
        String attributeUrl[] = new String[]{
                "cpu/limit",
                "network/tx_rate",
                "memory/limit",
                "memory/node_allocatable",
                "memory/major_page_faults_rate",
                "network/tx_errors_rate",
                "memory/major_page_faults",
                "memory/node_reservation",
                "memory/working_set",
                "cpu/request",
                "network/rx_errors_rate",
                "cpu/usage",
                "network/tx_errors",
                "memory/cache",
                "network/tx",
                "memory/page_faults_rate",
                "cpu/node_allocatable",
                "network/rx",
                "network/rx_rate",
                "memory/request",
                "cpu/usage_rate",
                "memory/node_utilization",
                "memory/rss",
                "network/rx_errors",
                "memory/page_faults",
                "memory/node_capacity",
                "cpu/node_capacity",
                "uptime",
                "cpu/node_reservation",
                "cpu/node_utilization"
        };

        //属性名
        String attributeName[] = new String[]{
                "cpuLimit",
                "networkTxRate",
                "memoryLimit",
                "memoryNodeAllocatable",
                "memoryMajorPageFaultsRate",
                "networkTxErrorsRate",
                "memoryMajorPageFaults",
                "memoryNodeReservation",
                "memoryWorkingSet",
                "cpuRequest",
                "networkRxErrorsRate",
                "cpuUsage",
                "networkTxErrors",
                "memoryCache",
                "networkTx",
                "memoryPageFaultsRate",
                "cpuNodeAllocatable",
                "networkRx",
                "networkRxRate",
                "memoryRequest",
                "cpuUsageRate",
                "memoryNodeUtilization",
                "memoryRss",
                "networkRxErrors",
                "memoryPageFaults",
                "memoryNodeCapacity",
                "cpuNodeCapacity",
                "upTime",
                "cpuNodeReservation",
                "cpuNodeUtilization"
        };

        //除memoryusage之外的所有属性获取数据
        for (int i = 0;i<attributeUrl.length;i++){
            String nodeUrlX = "http://10.60.38.181:10001/api/v1/model/nodes/rain-u2/metrics/"+attributeUrl[i]+"?start="+startTime+"&end="+endTime;
            String responseX = HttpRequest.get(nodeUrlX).body();

           //去掉json开头的无用数据，只留下【 】之内的数据
            String resJsonX = stringCut("[","]",responseX);

            JSONArray jsonArrayX = JSONArray.fromObject(resJsonX);

            int z = 1;

            for (int j = 0;j<jsonArrayX.size();j++){

                //用来判断是否有相同的timestamp
                int judge = 0;
                String tempJsonTime = jsonArrayX.getJSONObject(j).getString("timestamp");

                for (NodeData nodeData : nodeDatas){

                    //若时间相同，则将此属性的值存放到该对象里
                    if (nodeData.getTimeStamp().equals(tempJsonTime)){
                        //利用反射机制 提高泛化能力，减少不必要的代码量
                        Class<?> cls = nodeData.getClass();
                        Method setMed = cls.getMethod("set"+initcap(attributeName[i]),String.class);
                        Method getMed = cls.getMethod("get"+initcap(attributeName[i]));
                        setMed.invoke(nodeData,jsonArray.getJSONObject(j).getString("value"));
                        //若判断相同，则将judge值变 1
                        judge = 1;
                        break;
                    }
                }
                //若没出现过timestamp相同的情况，则新实例化一个对象，放到队尾
                if ( judge == 0 ){
                    NodeData nodeData = new NodeData();
                    nodeData.setTimeStamp(jsonArray.getJSONObject(j).getString("timestamp"));
                    //反射机制
                    Class<?> cls2 = nodeData.getClass();
                    Object obj2 = cls2.newInstance();
                    Method setMed2 = cls2.getMethod("set"+initcap(attributeName[i]),String.class);
                    setMed2.invoke(obj2,jsonArray.getJSONObject(j).getString("value"));
                    nodeDatas.add(nodeData);
                }

            }

        }

        String attributeNameAll[] = new String[]{
                "timeStamp",
                "cpuLimit",
                "networkTxRate",
                "memoryLimit",
                "memoryNodeAllocatable",
                "memoryMajorPageFaultsRate",
                "networkTxErrorsRate",
                "memoryMajorPageFaults",
                "memoryNodeReservation",
                "memoryWorkingSet",
                "cpuRequest",
                "networkRxErrorsRate",
                "cpuUsage",
                "networkTxErrors",
                "memoryCache",
                "networkTx",
                "memoryPageFaultsRate",
                "cpuNodeAllocatable",
                "networkRx",
                "networkRxRate",
                "memoryRequest",
                "cpuUsageRate",
                "memoryNodeUtilization",
                "memoryRss",
                "networkRxErrors",
                "memoryPageFaults",
                "memoryNodeCapacity",
                "cpuNodeCapacity",
                "upTime",
                "cpuNodeReservation",
                "memoryUsage",
                "cpuNodeUtilization"
        };

        //输出csv
        try {
            CsvWriter csvWriter= new CsvWriter(filePath,',',Charset.forName("UTF-8"));
            String[] headers = attributeNameAll;
            csvWriter.writeRecord(headers);
            //把对象的属性值填到数组里，然后再输出CSV
        for (NodeData nodeData : nodeDatas){
            String content[] = new String[attributeNameAll.length];
            for (int k=0;k<attributeNameAll.length;k++){
                //反射机制
                Class<?> cls1 = nodeData.getClass();
                Method getMed = cls1.getMethod("get"+initcap(attributeNameAll[k]));
                content[k] = (String)getMed.invoke(nodeData);
            }
            csvWriter.writeRecord(content);
        }
            csvWriter.close();

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    //将json 的String对象切割成起始为 [ ] 的String对象
    private static String stringCut(String startKey,String endKey, String str) {
        int a = str.indexOf(startKey);
        int b = str.indexOf(endKey);
        String res = str.substring(a,b+1);
        return res;
    }

    //将字符串首字母大写
    public static String initcap(String str){
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }

}

// 输入格式：
// 2018-05-07 13:14:00.000
// 2018-05-07 13:28:00.000
// datatext

