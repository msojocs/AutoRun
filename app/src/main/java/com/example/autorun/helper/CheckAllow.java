package com.example.autorun.helper;

import android.util.Log;

import org.runrun.utils.HTTP.HttpUtil2;

public class CheckAllow extends Thread{
    private static String TAG = CheckAllow.class.getSimpleName();

    @Override
    public void run() {
        super.run();

        try {
            String s = new HttpUtil2().doGet("https://task.jysafe.cn/task/unirun.txt");
            Log.i(TAG, "s = " + s);
            if(!s.equals("正常"))System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
