package org.runrun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API: v1/clubactivity/signInOrSignBack
 *
 * @Author jiyec
 * @Date 2021/11/15 18:19
 * @Version 1.0
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInOrSignBackBody {
    private Long activityId;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 签到或签退
     * 1：签到
     * 2：签退
     */
    private String signType;
    private Long studentId;
}
