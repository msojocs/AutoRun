package com.example.autorun.helper;

import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import org.runrun.utils.HTTP.HttpUtil2;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckAllow extends Thread{
    private static String TAG = CheckAllow.class.getSimpleName();

    @Override
    public void run() {
        super.run();

        try {
//            Log.i(TAG, "s = " + s);
            long time = new Date().getTime();
            String checkStr = time + "-unirun-" + SystemUtil.getSerial() + "-" + SystemUtil.getDeviceBrand() + "-" + SystemUtil.getSystemModel() + "-" + SystemUtil.getSystemVersion();
            String key = "unirun1234554321";
//            Log.i(TAG, checkStr);
            checkStr = AESUtil.Encrypt(checkStr, key);
//            Log.i(TAG, checkStr);
//            checkStr = AESUtil.Decrypt(checkStr, key);
//            Log.i(TAG, checkStr);
            String reqStr = Base64.encodeToString(checkStr.getBytes(StandardCharsets.UTF_8), 1);

            byte[] s1 = new HttpUtil2().doPostJson2Byte("http://task.jysafe.cn/task/unirun2.php", null, reqStr);
            String s = new String(s1);
//            Log.i(TAG, "s = " + s);
            String result = AESUtil.Decrypt(s, key);
//            Log.i(TAG, "result = " + result);
            String[] split = result.split("-");
            if(!(split[0].equals(time + "") && "ok".equals(split[1])))System.exit(-1);
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
