package org.runrun.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路径的单个位置点
 *
 * @Author jiyec
 * @Date 2021/10/16 15:16
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private int id;
    private String location;
    private int[] edge;
}
