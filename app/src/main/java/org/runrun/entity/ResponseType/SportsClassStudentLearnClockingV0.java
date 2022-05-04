package org.runrun.entity.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/11/15 18:41
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SportsClassStudentLearnClockingV0 {
    private Long classLearnId;
    private String clockingRange;
    private String startTime;
    private String endTime;
    private String latitude;
    private String longitude;
    private String planLearn;
    private String signBackStatus;
    private String signInStatus;
    private String signStatus;
    private String sportsClassId;
    private String sportsClassName;
}
