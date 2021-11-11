package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/10/17 10:44
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewRecordBody {
    String againRunStatus = "0";
    int againRunTime = 0;

    /**
     * APP版本
     */
    String appVersions = "1.8.0";
    /**
     * 手机品牌
     */
    String brand;
    /**
     * 手机型号
     */
    String mobileType;
    /**
     * 系统版本号
     */
    String sysVersions;
    /**
     * 跑步路线
     */
    String trackPoints;
    String distanceTimeStatus = "1";
    String innerSchool = "1";
    /**
     * 跑步距离
     */
    long runDistance;
    /**
     * 跑步时间
     */
    int runTime;
    /**
     * 用户ID
     */
    long userId;
    /**
     * 声纹验证
     */
    String vocalStatus = "1";
    /**
     * 学期
     */
    String yearSemester;
    /**
     * 记录日期
     */
    String recordDate;
    /**
     * 学校经纬度区间
     */
    String realityTrackPoints;
}
