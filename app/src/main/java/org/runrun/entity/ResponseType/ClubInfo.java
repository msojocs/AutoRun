package org.runrun.entity.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/11/14 22:07
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClubInfo {
    private Long clubActivityId;
    private String activityName;
    private String addressDetail;
    private String clubIntroduction;
    private Long signInStudent;
    private Long maxStudent;
    private String teacherName;
    private String startTime;
    private String endTime;
    private String optionStatus;
    private String fullActivity;
    private Integer cancelSign;
    private Long yearSemester;
    private Long activityItemId;
    private Long signStatus;
}
