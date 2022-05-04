package org.runrun.entity.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/11/17 19:27
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInTf {
    private Long activityId;
    private String activityName;
    private String activityType;
    private String address;
    private Integer continueTime;
    /**
     * 俱乐部开始时间
     */
    private String startTime;
    /**
     * 俱乐部结束时间
     */
    private String endTime;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 纬度
     */
    private String latitude;
    private Integer signBackLimitTime;
    private String signBackStatus;
    private String signInStatus;
    private String signInTime;
    private String signStatus;
}
