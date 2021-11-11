package org.example.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {

    /**
     * 读取文件
     * @param Path 文件路径
     * @return String 文件内容
     */
    public static String ReadFile(String Path) {
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr.toString();
    }
    /**
     * 读取文件
     * @return String 文件内容
     */
    public static String ReadFile(InputStream is) {
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            reader = new BufferedReader(inputStreamReader);
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                laststr.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr.toString();
    }
    public static boolean WriteFile(String file, String data){
        try {
            new File(new File(file).getParent()).mkdirs();
            OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }
}
