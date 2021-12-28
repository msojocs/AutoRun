package org.runrun.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @Author jiyec
 * @Date 2021/10/15 20:47
 * @Version 1.0
 **/
public class SignUtils {
    private static final String APPKEY = "389885588s0648fa";
    private static final String APPSECRET = "56E39A1658455588885690425C0FD16055A21676";

    public static String get(Map<String, String> query, String body) throws UnsupportedEncodingException {
        String str = null;
        // 注意，TreeSet是有序集合，且默认为正序排列，即{a, b, c, d [,...]}
        if(query == null)query = new HashMap<>();
        // 待签名字符串
        StringBuilder sb = new StringBuilder();
        TreeSet<String> treeSet = new TreeSet<>(query.keySet());
        // 开始迭代所有请求参数
        for (String key : treeSet) {
            // key 为参数名
            // 获取参数名对应的值
            String value = query.get(key);
            if (value != null) {
                // key 参数名， str9 参数值
                // str9非空，追加str8 str9在sb之后
                sb.append(key);
                sb.append(value);
            }
        }
        // 追加APPKEY
        sb.append(APPKEY);
        // 追加APPSECRET
        sb.append(APPSECRET);
        if (body != null) {
            sb.append(body);
        }
        // 同sb
        String sb2 = sb.toString(), str3;
        CharSequence charSequence = sb2;
        boolean z2 = false;
        if (!(charSequence.length() == 0)) {
            // 本级语句块替换一些字符为空字符，即删除部分字符，这将导致参与MD5运算的字符串有所差异
            // z2 为是否发生过替换操作
            // 删除空格
            if (sb2.contains(" ")) {
                sb2 = sb2.replaceAll(" ", "");
                z2 = true;
            } else {
                str3 = sb2;
            }
            // 删除~
            if (sb2.contains("~")) {
                sb2 = sb2.replaceAll("~", "");
                z2 = true;
            }
            // 删除!
            if (sb2.contains("!")) {
                sb2 = sb2.replaceAll("!", "");
                z2 = true;
            }
            // 删除(
            if (sb2.contains("(")) {
                sb2 = sb2.replaceAll("\\(", "");
                z2 = true;
            }
            // 删除)
            if (sb2.contains(")")) {
                sb2 = sb2.replaceAll("\\)", "");
                z2 = true;
            }
            // 删除'
            if (sb2.contains("'")) {
                sb2 = sb2.replaceAll("'", "");
                z2 = true;
            }
            if (z2) {
                sb2 = URLEncoder.encode(sb2, "utf-8");
            }
        }
        if (z2) {
            StringBuilder sb3 = new StringBuilder();
            // 使用替换结果 sb2 计算MD5
            String encodeByMD5 = MD5Utils.stringToMD5(sb2);
            String str2 = null;
            if (encodeByMD5 != null) {
                // 转换为大写
                str2 = encodeByMD5.toUpperCase();
            }
            sb3.append(str2);
            // MD5追加编码
            sb3.append("encodeutf8");
            str = sb3.toString();
        } else {
            // 没有发生替换，使用sb
            String sb4 = sb.toString();
            // 使用sb计算MD5
            String encodeByMD52 = MD5Utils.stringToMD5(sb4);
            if (encodeByMD52 != null) {
                // 转换为大写
                str = encodeByMD52.toUpperCase();
            }
        }
        return str;
    }
}
