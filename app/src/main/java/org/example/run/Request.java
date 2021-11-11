package org.example.run;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import org.apache.hc.core5.http.ParseException;
import org.example.entity.AppConfig;
import org.example.entity.NewRecordBody;
import org.example.entity.Response;
import org.example.entity.ResponseType.RunStandard;
import org.example.entity.ResponseType.SchoolBound;
import org.example.entity.ResponseType.UserInfo;
import org.example.utils.HTTP.HttpUtil2;
import org.example.utils.JsonUtils;
import org.example.utils.MD5Utils;
import org.example.utils.SignUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author jiyec
 * @Date 2021/10/17 10:49
 * @Version 1.0
 **/
public class Request {
    private final static HttpUtil2 http = new HttpUtil2();
    private final String appKey = "389885588s0648fa";
    private String token;
    private AppConfig config;

    public Request(String token, AppConfig config){
        this.token = token;
        this.config = config;
    }

    public UserInfo login(String phone, String password) throws IOException {
        String pass = MD5Utils.stringToMD5(password);
        String API = "https://run-lb.tanmasports.com/v1/auth/login/password";
        try {
            Map<String, String> body = new HashMap<>();
            body.put("appVersion", config.getAppVersion());
            body.put("brand", config.getBrand());
            body.put("deviceToken", config.getDeviceToken());
            body.put("deviceType", config.getDeviceType());
            body.put("mobileType", config.getMobileType());
            body.put("password", pass);
            body.put("sysVersion", config.getSysVersion());
            body.put("userPhone", phone);

            Map<String, String> headers = new HashMap<>();
            String bodyStr = JsonUtils.obj2String(body);
            String sign = SignUtils.get(null, bodyStr);
            headers.put("sign", sign);
            headers.put("token", token);
            headers.put("appkey", appKey);
            headers.put("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = http.doPostJson2Byte(API, headers, bodyStr);
            Response<UserInfo> userInfoResponse = JsonUtils.string2Obj(new String(bytes), new TypeReference<Response<UserInfo>>() {});
            int code = userInfoResponse.getCode();
            if(code == 10000){
                UserInfo userInfo = userInfoResponse.getResponse();
                this.token = userInfo.getOauthToken().getToken();
                return userInfo;
            }else{
                throw new RuntimeException(userInfoResponse.getMsg());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    public long getUserId(){
        String API = "https://run-lb.tanmasports.com/v1/auth/query/token";
        try {
            Map<String, String> headers = new HashMap<>();
            String sign = SignUtils.get(null, null);
            headers.put("sign", sign);
            headers.put("token", token);
            headers.put("appkey", appKey);
            headers.put("Content-Type", "application/json; charset=UTF-8");
            String tokenInfo = http.doGet2(API, headers);
            Response<UserInfo> userInfoResponse = JsonUtils.string2Obj(tokenInfo, new TypeReference<Response<UserInfo>>() {});
            int code = userInfoResponse.getCode();
            if(code == 10000){
                return userInfoResponse.getResponse().getUserId();
            }else{
                throw new RuntimeException(userInfoResponse.getMsg());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public SchoolBound[] getSchoolBound(){

        String API = "https://run-lb.tanmasports.com/v1/unirun/querySchoolBound?schoolId=3680";
        try {
            Map<String, String> headers = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            params.put("schoolId", "3680");
            String sign = SignUtils.get(params, null);
            headers.put("sign", sign);
            headers.put("token", token);
            headers.put("appkey", appKey);
            headers.put("Content-Type", "application/json; charset=UTF-8");
            String tokenInfo = http.doGet2(API, headers);
            Response<SchoolBound[]> schoolBoundResponse = JsonUtils.string2Obj(tokenInfo, new TypeReference<Response<SchoolBound[]>>() {});
            if(schoolBoundResponse.getCode() != 10000){
                throw new RuntimeException(schoolBoundResponse.getMsg());
            }
            return schoolBoundResponse.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RunStandard getRunStandard(){

        String API = "https://run-lb.tanmasports.com/v1/unirun/query/runStandard?schoolId=3680";
        try {
            Map<String, String> headers = new HashMap<>();
            Map<String, String> params = new HashMap<>();
            params.put("schoolId", "3680");
            String sign = SignUtils.get(params, null);
            headers.put("sign", sign);
            headers.put("token", token);
            headers.put("appkey", appKey);
            headers.put("Content-Type", "application/json; charset=UTF-8");
            String tokenInfo = http.doGet2(API, headers);
            Response<RunStandard> standardResponse = JsonUtils.string2Obj(tokenInfo, new TypeReference<Response<RunStandard>>() {});
            if(standardResponse.getCode() != 10000){
                throw new RuntimeException(standardResponse.getMsg());
            }
            return standardResponse.getResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String recordNew(NewRecordBody body){
        String API = "https://run-lb.tanmasports.com/v1/unirun/save/run/record/new";
        try {
            Map<String, String> headers = new HashMap<>();
            String bodyStr = JsonUtils.obj2String(body);
            String sign = SignUtils.get(null, bodyStr);
            headers.put("sign", sign);
            headers.put("token", token);
            headers.put("appkey", appKey);
            headers.put("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = http.doPostJson2Byte(API, headers, bodyStr);
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
