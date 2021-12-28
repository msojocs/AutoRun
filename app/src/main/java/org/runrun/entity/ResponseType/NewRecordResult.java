package org.runrun.entity.ResponseType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/10/17 13:24
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewRecordResult {
    String resultStatus;
    String resultDesc;
    String overSpeedWarn;
    String warnContent;
    long recordId;
}
