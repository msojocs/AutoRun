package org.runrun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应体
 *
 * @Author jiyec
 * @Date 2021/10/17 12:03
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<T> {
    private int code;
    private String msg;
    private T response;
}
