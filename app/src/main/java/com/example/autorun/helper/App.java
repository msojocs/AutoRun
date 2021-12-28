package com.example.autorun.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.TintContextWrapper;

import com.example.autorun.ui.login.LoginActivity;
import com.fasterxml.jackson.core.type.TypeReference;

import org.apache.hc.core5.http.ParseException;
import org.runrun.entity.AppConfig;
import org.runrun.entity.Location;
import org.runrun.entity.NewRecordBody;
import org.runrun.entity.Response;
import org.runrun.entity.ResponseType.NewRecordResult;
import org.runrun.entity.ResponseType.RunStandard;
import org.runrun.entity.ResponseType.SchoolBound;
import org.runrun.entity.ResponseType.UserInfo;
import org.runrun.run.Request;
import org.runrun.utils.FileUtil;
import org.runrun.utils.JsonUtils;
import org.runrun.utils.TrackUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import lombok.Setter;

/**
 * Hello world!
 *
 */
public class App extends Thread
{
    AppConfig config;
    @Setter
    private InputStream mapInput;

    @Setter
    private TextView resultArea;
    @Setter
    ProgressBar loadingProgressBar;
    public static String ERROR;

    public App(AppConfig config) {
        this.config = config;
    }

    @SuppressLint("DefaultLocale")
    public void runSub() throws IOException, ParseException {

        appendMsg("开始");
        // ==========配置 START==============
        String phone = config.getPhone();
        String password = config.getPassword();
        String token = "";
        int schoolSite = 0;     // 0航空港，1龙泉暂不支持
        long runDistance = config.getDistance();        // 路程米
        int runTime = config.getRunTime();               // 时间分钟

        // 型号仓库： https://github.com/KHwang9883/MobileModels
        // ==========配置 END==============

        if (config.getBrand().length() == 0) {
            appendMsg("请配置手机型号信息");
            return;
        }
        // 计算平均配速，防止跑太快
        double average = 1.0 * runTime / runDistance * 1000;
        if (average < 6) {
            String[] notice = {
                    "我认为这种事情是不可能的",
                    "太快了",
                    "要死了",
                    "你正在自毁",
                    "你正在自残",
                    "你得锻炼正造成身体上的损伤",
                    "六分是养身",
                    "七分是自娱",
                    "八分是治愈"
            };
            appendMsg("八分是治愈，七分是自娱，六分是养身，五分是自伤，四分是自残，三分是自毁。");
            appendMsg(String.format("你的配速是：%.2f 分钟/公里, %s", average, notice[(int) average]));
            return;
        }
        appendMsg(String.format("平均配速：%.2f\n", average));

//        if(config.getRunTime() > 0)return;

        Request request = new Request(token, config);
        appendMsg("开始登录");
        UserInfo userInfo = request.login(phone, password);
        if(userInfo == null ) {
            appendMsg("登录失败");
            return;
        }
        long userId = userInfo.getUserId();
        if (userId != -1) {
            appendMsg("获取跑步标准");
            RunStandard runStandard = request.getRunStandard(userInfo.getSchoolId());
            appendMsg("获取学校经纬度区域信息");
            SchoolBound[] schoolBounds = request.getSchoolBound(userInfo.getSchoolId());

            appendMsg("生成跑步数据");
            // 新增跑步数据
            NewRecordBody recordBody = new NewRecordBody();
            recordBody.setUserId(userId);
            recordBody.setAppVersions(config.getAppVersion());
            recordBody.setBrand(config.getBrand());
            recordBody.setMobileType(config.getMobileType());
            recordBody.setSysVersions(config.getSysVersion());
            recordBody.setRunDistance(runDistance);
            recordBody.setRunTime(runTime);
            recordBody.setYearSemester(runStandard.getSemesterYear());
            recordBody.setRealityTrackPoints(schoolBounds[schoolSite].getSiteBound() + "--");

            // 今天日期 年-月-日
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd");
            Date date = new Date();
            String formatTime = sdf.format(date);
            recordBody.setRecordDate(formatTime);

            // 生成跑步数据
            String tack = genTack(runDistance);
            recordBody.setTrackPoints(tack);

            //发送数据
            appendMsg("提交跑步数据");
            String result = request.recordNew(recordBody);
            Response<NewRecordResult> response = JsonUtils.string2Obj(result, new TypeReference<Response<NewRecordResult>>() {
            });
            appendMsg("");
            appendMsg("返回原始数据：" + result);
            appendMsg("解析数据：");
            appendMsg("跑步结果：" + response.getCode() + " - " + response.getMsg());
            NewRecordResult response1 = response.getResponse();
            appendMsg("生成的跑步ID：" + response1.getRecordId());
            appendMsg("结果状态：" + response1.getResultStatus());
            appendMsg("结果描述：" + response1.getResultDesc());
            appendMsg("超速警告次数：" + response1.getOverSpeedWarn());
            appendMsg("警告内容：" + response1.getWarnContent());
        } else {
            appendMsg("用户Id获取失败");
        }
    }

    public void run(){
        try{
            runSub();
        }catch (Exception e){
            e.printStackTrace();
            String msg;
            if(e instanceof RuntimeException) {
                StackTraceElement traceElement = e.getStackTrace()[0];
                msg = e.getMessage() + "\n异常来源：" +traceElement.getClassName() + " - line:" + traceElement.getLineNumber();
            }else{
                msg = e.getMessage();
            }
            appendMsg(msg);
        }finally {
            stopLoading();
        }
    }

    public void appendMsg(String msg){
        Context context = resultArea.getContext();

        Activity activity = null;
        if(context instanceof LoginActivity) {
            activity = (Activity) context;
        }else if(context instanceof TintContextWrapper){
            activity = (Activity)((TintContextWrapper) context).getBaseContext();
        }
        if(activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultArea.append("\n" + msg);
                }
            });
    }

    public String genTack(long distance) {
        if(mapInput == null)
            mapInput = org.runrun.App.class.getResourceAsStream("/map.json");
        String json = FileUtil.ReadFile(mapInput);
        try {
            mapInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (json.length() == 0) {
            System.out.println("配置读取失败");
            return null;
        }
        Location[] locations = JsonUtils.string2Obj(json, Location[].class);
        return TrackUtils.gen(distance, locations);
    }

    public void stopLoading(){
        Context context = resultArea.getContext();
        // 4.4 TintContextWrapper
        // 5.1 LoginActivity

        Activity activity = null;
        if(context instanceof LoginActivity) {
            activity = (Activity) context;
        }else if(context instanceof TintContextWrapper){
            activity = (Activity)((TintContextWrapper) context).getBaseContext();
        }

        if(activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingProgressBar.setVisibility(View.INVISIBLE);
                }
            });
    }
}
