package org.example.entity.ResponseType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/10/17 12:04
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunStandard {
    private long standardId;
    long schoolId;
    int boyOnceTimeMin;
    int boyOnceTimeMax;
    String semesterYear;
}
