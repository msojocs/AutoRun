package org.runrun.entity.ResponseType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/11/24 13:34
 * @Version 1.0
 **/
@Data
@NoArgsConstructor
public class MyActivityItem {
    private Long clubActivityId;
    private String activityName;
    private String activityStatus;
    private String addressDetail;
    private String clubIntroduction;
    private Long configurationTimeId;
    private Integer signInStudent;
    private Integer maxStudent;
    private String teacherName;
    private String startTime;
    private String endTime;
    private String mmdd;
    private Long nextClubActivityId;
    private String nextStartTime;
    private String nextEndTime;
    private String nextMmdd;
}
