package org.runrun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/10/17 13:39
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfig {
    private String phone;
    String password;
    private StringBuffer token = new StringBuffer();

    long distance;
    int runTime;
    String appVersion;
    String brand;
    String deviceToken = "";
    String deviceType = "1";
    String mobileType;
    String sysVersion;

}
