package org.runrun.entity.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jiyec
 * @Date 2021/11/15 0:33
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinClubResult {
    private String message;
    private String status;
}
