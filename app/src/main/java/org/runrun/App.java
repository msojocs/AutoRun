package org.runrun;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Date;
import java.util.Scanner;

/**
 * 运行主体
 * 说明：
 * 本程序仅供学习交流使用，请在下载后24小时内及时删除。
 * 本程序不提供后续更新服务。
 * 若由使用本程序造成包括但不限于校方警告、课程分数计0、封号、勒令退学等不良后果，一切责任由使用者承当。
 * 使用本程序即代表使用者同意以上条款。
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        // ==========配置 START==============
        boolean input = true;
        String phone = "";
        String password = "";
        String token = "";
        int schoolSite = 0;     // 0航空港，1龙泉
        long runDistance = 0;        // 路程米
        int runTime = 0;               // 时间分钟
        // 型号仓库： https://github.com/KHwang9883/MobileModels
        AppConfig config = new AppConfig() {{
            setAppVersion("1.8.0");     // APP版本，一般不做修改
            setBrand("");         // 手机品牌
            setMobileType("");   // 型号
            setSysVersion("10");        // 系统版本
        }};
        // ==========配置 END==============

        if (config.getBrand().length() == 0) {
            System.out.println("请配置手机型号信息");
            System.exit(-1);
        }
        if (input) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("账号（手机）：");
            phone = scanner.next();
            System.out.print("密码：");
            password = scanner.next();
            System.out.print("跑步路程(米)：");
            runDistance = scanner.nextLong();        // 路程米
            System.out.print("跑步时间(分钟)：");
            runTime = scanner.nextInt();               // 时间分钟
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
            System.out.println("八分是治愈，七分是自娱，六分是养身，五分是自伤，四分是自残，三分是自毁。");
            System.out.printf("你的配速是：%.2f 分钟/公里, %s", average, notice[(int) average]);
            System.exit(-1);
        }
        System.out.printf("平均配速：%.2f\n", average);

        Request request = new Request(token, config);
        UserInfo userInfo = request.login(phone, password);
        long userId = userInfo.getUserId();
        if (userId != -1) {
            RunStandard runStandard = request.getRunStandard(userInfo.getSchoolId());
            SchoolBound[] schoolBounds = request.getSchoolBound(userInfo.getSchoolId());
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
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd");
            Date date = new Date();
            String formatTime = sdf.format(date);
            recordBody.setRecordDate(formatTime);

            // 生成跑步数据
            String tack = genTack(runDistance);
            recordBody.setTrackPoints(tack);

            //发送数据
            String result = request.recordNew(recordBody);
            Response<NewRecordResult> response = JsonUtils.string2Obj(result, new TypeReference<Response<NewRecordResult>>() {
            });
            System.out.println(result);
        } else {
            System.out.println("用户Id获取失败");
        }
    }

    public static String genTack(long distance) {
        InputStream resourceAsStream = App.class.getResourceAsStream("/map.json");
        String json = FileUtil.ReadFile(resourceAsStream);
        if (json.length() == 0) {
            System.out.println("配置读取失败");
            return null;
        }
        Location[] locations = JsonUtils.string2Obj(json, Location[].class);
        return TrackUtils.gen(distance, locations);
    }
}