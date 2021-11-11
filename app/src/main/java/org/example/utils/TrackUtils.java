package org.example.utils;

import org.example.entity.Location;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * 路径生成工具
 * 使用无向图进行自动寻路
 *
 * @Author jiyec
 * @Date 2021/10/16 15:21
 * @Version 1.0
 **/
public class TrackUtils {
    /**
     * 路径生成算法
     *
     * @param distance
     * @param locations
     * @return
     */
    public static String gen(long distance, Location[] locations){
        int currentDistance = 0;
        // 随机起始点
        int startIndex = (int)(locations.length * Math.random());
        Location startLocation = locations[startIndex];
        Location currentLocation = startLocation;
        // 路径集合
        List<String> result = new LinkedList<>();

        // 时间推前30分钟
        long startTime = new Date().getTime() - 30 * 60 * 1000;
        int lastIndex = -1;

        while (currentDistance < distance){
            String current = currentLocation.getLocation();
            int[] edge = currentLocation.getEdge();

            // 选择下一个结点
            if(edge.length == 0){
                System.out.println("edge为空");
            }
            int randInt = randInt(0, edge.length);
            int edgeIndex = edge[randInt];
            // 尽量不往回走
            if(edgeIndex == lastIndex){
                edgeIndex = edge[(randInt + 1)%edge.length];
            }
            Location next = locations[edgeIndex];

            String[] start = current.split(",");
            String[] end = next.getLocation().split(",");
            double[] startData = new double[]{Double.parseDouble(start[0]), Double.parseDouble(start[1])};
            double[] endData = new double[]{Double.parseDouble(end[0]), Double.parseDouble(end[1])};

            double goDistance = CalculateDistance(startData, endData);
            currentDistance += goDistance;

            result.add(String.format("%s-%s-%s-%.1f", start[0], start[1], startTime, randAccuracy()));

            double[] lastRandPos = startData;
            for (int i = 0; i < 10; i++) {
                double[] newRandPos = randPos(lastRandPos, endData);
                double distance1 = CalculateDistance(lastRandPos, newRandPos);
                lastRandPos = newRandPos;
                // 距离/速度 = 时间 (毫秒)  |  随机速度 1-5m/s
                startTime += distance1 / randInt(1, 5) * 1000;
                result.add(String.format("%s-%s-%s-%.1f", lastRandPos[0], lastRandPos[1], startTime, randAccuracy()));
            }
            double distance1 = CalculateDistance(lastRandPos, endData);
            // 距离/速度 = 时间 (毫秒)  |  随机速度 1-5m/s
            startTime += distance1 / randInt(1, 5) * 1000;
            result.add(String.format("%s-%s-%s-%.1f", end[0], end[1], startTime, randAccuracy()));

            lastIndex = currentLocation.getId();
            currentLocation = next;
        }
        startTime += randInt(5, 10) * 1000L;
        String replace = currentLocation.getLocation().replace(',', '-');
        result.add(String.format("%s-%s-%.1f", replace, startTime, randAccuracy()));
        return JsonUtils.obj2String(result);
    }

    public static int randInt(int end) {
        return randInt(0, end);
    }
    // [start, end)
    public static int randInt(int start, int end){
        if(start == end) System.out.println("范围空!");
        int len = end - start;
        return (int)(start + len * Math.random());
    }

    /**
     *
     * @param start "经度,维度"
     * @param end   "经度,维度"
     * @return 距离，单位：米
     */
    public static double CalculateDistance(double[] start, double[] end){
        GlobalCoordinates source = new GlobalCoordinates(start[1], start[0]);
        GlobalCoordinates target = new GlobalCoordinates(end[1], end[0]);
        return new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, source, target).getEllipsoidalDistance();
    }

    /**
     * 随机取一个经过点
     * @param start {经度, 维度}
     * @return
     */
    public static double[] randPos(double[] start, double[] end){
        // y = ax + b
        double dy = end[0] - start[0];
        if(dy == 0)dy = 0.00000001;
        double a = (end[1] - start[1])/ (dy);
        a = a + a*0.2; // 斜率波动
        double b = end[1] - a * end[0];
        double x = start[0] + (dy) * Math.random();

        double[] result = new double[]{};
        try{
            x = new BigDecimal(x).setScale(6, BigDecimal.ROUND_DOWN).doubleValue();
            double y = new BigDecimal(a * x + b).setScale(6, BigDecimal.ROUND_DOWN).doubleValue();
            result = new double[]{x, y};
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
    public static double randAccuracy(){
        return 10 * Math.random();
    }
}
