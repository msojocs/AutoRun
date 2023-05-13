package com.example.autorun.helper;

import android.os.Build;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import org.runrun.utils.HTTP.HttpUtil2;
import org.runrun.utils.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
public class CheckAllow extends Thread{
    private static String TAG = CheckAllow.class.getSimpleName();
    private String androidId = null;
    private String uuid = null;
    private String apkVersion = null;
    private TextView resultArea;
    private Consumer<Map<String, Object>> consumer;

    @Override
    public void run() {
        super.run();

        try {
//            Log.i(TAG, "s = " + s);
            long time = new Date().getTime();
            Map<String, Object> data = new HashMap<>();
            data.put("time", time);
            data.put("type", "unirun");
            Log.i(TAG, "serial: " + SystemUtil.getSerial());
            Log.i(TAG, "android id: " + androidId);
            data.put("serial", SystemUtil.getSerial());
            data.put("android_id", androidId);
            data.put("device_brand", SystemUtil.getDeviceBrand());
            data.put("system_model", SystemUtil.getSystemModel());
            data.put("system_version", SystemUtil.getSystemVersion());
            data.put("apk_version", apkVersion);
            String checkStr = JsonUtils.obj2String(data);
            String key = "unirun1234554321";
            Log.i(TAG, "info:" + checkStr);
            checkStr = AESUtil.Encrypt(checkStr, key);
//            Log.i(TAG, checkStr);
//            checkStr = AESUtil.Decrypt(checkStr, key);
//            Log.i(TAG, checkStr);
            String reqStr = Base64.encodeToString(checkStr.getBytes(StandardCharsets.UTF_8), 1);

            byte[] s1 = new HttpUtil2().doPostJson2Byte("http://task.jysafe.cn/task/unirun3.php", null, reqStr);
            String s = new String(s1);
            Log.i(TAG, "s = " + s);
            String result = AESUtil.Decrypt(s, key);
            Map<String, Object> ret = JsonUtils.string2Obj(result, Map.class);

//            Log.i(TAG, "result = " + result);
            long retTime = (long)ret.get("time");

            if(!(retTime == time && "ok".equals(ret.get("result"))))
                System.exit(-1);
            if (ret.containsKey("message") && consumer != null) {
                Looper.prepare();

                String message = (String) ret.get("message");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    consumer.accept(ret);
                }
                resultArea.append("\n" + message);
                Looper.loop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    public String toBinary(String str){
        char[] strChar = str.toCharArray();
        StringBuilder result= new StringBuilder();
        for (char c : strChar) {
            result.append(Integer.toBinaryString(c)).append(" ");
        }
        return result.toString();
    }
    //将二进制字符串转换成int数组
    public int[] BinstrToIntArray(String binStr) {
        char[] temp=binStr.toCharArray();
        int[] result=new int[temp.length];
        for(int i=0;i<temp.length;i++) {
            result[i]=temp[i]-48;
        }
        return result;
    }

    //将二进制转换成字符
    public char BinstrToChar(String binStr){
        int[] temp=BinstrToIntArray(binStr);
        int sum=0;
        for(int i=0; i<temp.length;i++){
            sum +=temp[temp.length-1-i]<<i;
        }
        return (char)sum;
    }
    public String BinstrToStr(String binStr){
        String[] tempStr=binStr.split(" ");
        char[] tempChar=new char[tempStr.length];
        for(int i=0;i<tempStr.length;i++) {
            tempChar[i]=BinstrToChar(tempStr[i]);
        }
        return String.valueOf(tempChar);
    }
}
