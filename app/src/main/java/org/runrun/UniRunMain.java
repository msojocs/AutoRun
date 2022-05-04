package org.runrun;

import org.runrun.entity.AppConfig;
import org.runrun.entity.Response;
import org.runrun.entity.ResponseType.ClubInfo;
import org.runrun.entity.ResponseType.SignInTf;
import org.runrun.entity.ResponseType.UserInfo;
import org.runrun.entity.SignInOrSignBackBody;
import org.runrun.run.Request;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运行主体
 * 说明：
 * 本程序仅供学习交流使用，请在下载后24小时内及时删除。
 * 本程序不提供后续更新服务。
 * 若由使用本程序造成包括但不限于校方警告、课程分数计0、封号、勒令退学等不良后果，一切责任由使用者承当。
 * 使用本程序即代表使用者同意以上条款。
 */
@Slf4j
public class UniRunMain {
    private static final AppConfig config = new AppConfig() {{
//        setAppVersion(SpringUtil.getValue("unirun.version"));     // APP版本，一般不做修改
        setBrand("realme");         // 手机品牌
        setMobileType("RMX2117");   // 型号
        setSysVersion("10");        // 系统版本
    }};

    public static void main(String[] args) {
//        Response response = UniRunMain.signInOrSignBack(new StringBuffer(), "", "");
//        log.info("{}", response);
//        String token = "1243489ade4c457702e7c9c7fe2698a0";
//        AppConfig config = new AppConfig() {{
//            setAppVersion("1.8.3");     // APP版本，一般不做修改
//            setBrand("realme");         // 手机品牌
//            setMobileType("RMX2117");   // 型号
//            setSysVersion("10");        // 系统版本
//        }};
//        Request request = new Request(token, config);
//        //UserInfo userInfo = request.login("", "1222");
//        UserInfo userInfo = request.getUserInfo().getResponse();
//
//        // 今天日期 年-月-日
//        SimpleDateFormat sdf = new SimpleDateFormat();
//        sdf.applyPattern("yyyy-MM-dd");
//        Date date = new Date();
//        String today = sdf.format(date);
//        List<ClubInfo> activityList = request.getActivityList(String.valueOf(userInfo.getStudentId()), today);
//        List<SportsClassStudentLearnClockingV0> mySportsClassClocking = request.getMySportsClassClocking();
//        log.info("{}", mySportsClassClocking);
    }

    public static Response<UserInfo> checkAccount(String phone, String password) throws IOException {

        Request request = new Request("", config);
        return request.login(phone, password);
    }
    public static List<ClubInfo> getAvailableActivityList(StringBuffer token, String phone, String password) throws IOException {

        Request request = new Request(token.toString(), config);
        Response<UserInfo> userInfoResponse = request.getUserInfo();
        //更新token
        if(userInfoResponse.getCode() != 10000) {
            log.info("token无效，更新");
            userInfoResponse = request.login(phone, password);
            token.delete(0, token.length());
            token.append(request.getToken());
        }
        UserInfo userInfo = userInfoResponse.getResponse();
        List<ClubInfo> list = new ArrayList<>();
        if (userInfo != null) {
            long studentId = userInfo.getStudentId();
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyy-MM-dd");
            Date date = new Date(new Date().getTime() + 1000 * 6 * 24 * 60 * 60);
            String today = sdf.format(date);
            List<ClubInfo> activityList = request.getActivityList(String.valueOf(studentId), today);

            for (ClubInfo clubInfo : activityList) {
                if (clubInfo.getSignInStudent() < clubInfo.getMaxStudent()) {
                    list.add(clubInfo);
                }
            }
        } else {
            log.error("用户Id获取失败");
        }
        return list;
    }

    public static Response autoJoinClub(StringBuffer token, String phone, String password, String location, String keyword) throws IOException {

        Request request = new Request(token.toString(), config);
        Response<UserInfo> userInfoResponse = request.getUserInfo();
        //更新token
        if(userInfoResponse.getCode() != 10000) {
            log.info("token无效，更新");
            userInfoResponse = request.login(phone, password);
            token.delete(0, token.length());
            token.append(request.getToken());
        }
        UserInfo userInfo = userInfoResponse.getResponse();
        if (userInfo == null) {
            log.info("用户信息获取失败：{}", userInfoResponse);
            return userInfoResponse;
        }
        SignInTf signInTf = request.getSignInTf(String.valueOf(userInfo.getStudentId()));
        log.info("signInTf：{}", signInTf);
        if (signInTf != null && signInTf.getActivityId() != null) {
            log.info("有将要进行的俱乐部活动：{}", signInTf);
            return null;
        }

        // 获取俱乐部列表
        List<ClubInfo> availableActivityList = new ArrayList<>();
        long studentId = userInfo.getStudentId();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");
        Date date = new Date(new Date().getTime() + 1000 * 6 * 24 * 60 * 60);
        String today = sdf.format(date);
        List<ClubInfo> activityList = request.getActivityList(String.valueOf(studentId), today);
        for (ClubInfo clubInfo : activityList) {
            if (clubInfo.getSignInStudent() < clubInfo.getMaxStudent()) {
                availableActivityList.add(clubInfo);
            }
        }
        //没有可以参加的俱乐部
        if(availableActivityList.size() == 0)return null;

        // 筛选关键词俱乐部
        List<ClubInfo> keyActList = availableActivityList.stream().filter(activity -> {
            boolean result = activity.getActivityName().contains(location);
            if (keyword != null)
                result = result && activity.getActivityName().contains(keyword);
            return result;
        }).collect(Collectors.toList());
        // 空
        if (keyActList.size() == 0) {
            return new Response() {{
                setMsg(String.format("没有找到可加入的俱乐部\n你的校区：%s\n你的关键词：%s", location, keyword));
            }};
        }

        log.info("尝试加入：{}", keyActList.get(0));
        // 取第一个
        Long activityId = keyActList.get(0).getClubActivityId();
        // 加入
        return request.joinClub(String.valueOf(studentId), String.valueOf(activityId));
    }

    /**
     * UniRun 签到签退
     *
     * @param token token
     * @param phone 手机号
     * @param password 密码
     * @return null-非可签到签退状态 | Response
     */
    public static Response signInOrSignBack(StringBuffer token, String phone, String password) throws IOException {

        Request request = new Request(token.toString(), config);
        Response<UserInfo> userInfoResponse = request.getUserInfo();
        //更新token
        if(userInfoResponse.getCode() != 10000) {
            log.info("token无效，更新");
            userInfoResponse = request.login(phone, password);
            token.delete(0, token.length());
            token.append(request.getToken());
        }
        UserInfo userInfo = userInfoResponse.getResponse();

        if (userInfo != null) {
            Long studentId = userInfo.getStudentId();
            SignInTf signInTf = request.getSignInTf(String.valueOf(studentId));
            log.info("待签到俱乐部：{}", signInTf);
            String signStatus = signInTf.getSignStatus();
            String signInStatus = signInTf.getSignInStatus();
            String signBackStatus = signInTf.getSignBackStatus();

            if ("1".equals(signInStatus) && "1".equals(signBackStatus)) return null;

            String signType;
            if ("1".equals(signStatus)) {
                //    可签到
                signType = "1";
            } else if ("1".equals(signInStatus) && "2".equals(signStatus)) {
                //    可签退
                signType = "2";
            } else {
                log.info("非可签到签退状态，或没有可签到项目");
                return null;
            }

            SignInOrSignBackBody signInOrSignBackBody = new SignInOrSignBackBody(
                    signInTf.getActivityId(),
                    signInTf.getLatitude(),
                    signInTf.getLongitude(),
                    signType,
                    studentId);

            return request.signInOrSignBack(signInOrSignBackBody);
        } else {
            return userInfoResponse;
        }
    }
}