package com.example.autorun.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

/**
 * 系统工具类
 * Created by zhuwentao on 2016-07-18.
 */
public class SystemUtil {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机序列号
     *
     * @return 序列号
     */
    public static String getSerial() {
        return Build.SERIAL;
    }

    public static String getAndroidId (Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 得到全局唯一UUID，保存数据到系统数据库中：Settings.System
     */
    public static String getUUID(Context context) {
        String uniqueIdentificationCode = "";
        //首先判断系统版本，高于6.0，则需要动态申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.System.canWrite(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } else {
                uniqueIdentificationCode = Settings.System.getString(context.getContentResolver(), "uniqueIdentificationCode");
            }
        } else {
            //获取系统配置文件中的数据，第一个参数固定的，但是需要上下文，第二个参数是之前保存的Key，第三个参数表示如果没有这个key的情况的默认值
            uniqueIdentificationCode = Settings.System.getString(context.getContentResolver(), "uniqueIdentificationCode");
        }

        if (TextUtils.isEmpty(uniqueIdentificationCode)) {
            uniqueIdentificationCode = System.currentTimeMillis() + UUID.randomUUID().toString().substring(20).replace("-", "");
            //设置系统配置文件中的数据，第一个参数固定的，但是需要上下文，第二个参数是保存的Key，第三个参数是保存的value
            Settings.System.putString(context.getContentResolver(), "uniqueIdentificationCode", uniqueIdentificationCode);
        }
        Log.i("getDeviceUuidId", "getDeviceUuidId: uniqueIdentificationCode = " + uniqueIdentificationCode);

        return uniqueIdentificationCode;
    }

}
