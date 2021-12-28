package org.runrun.utils.HTTP;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.util.Map;

/**
 * 网络请求工具类[公共]
 *
 * @author jiyec
 */
public class HttpUtil {

    private static final CustomCookieStore httpCookieStore;
    private static final HttpClientContext defaultContext;
    private static final RequestConfig.Builder unBuildConfig;
    public static final HttpUtil2 HTTP = new HttpUtil2();

    // 采用静态代码块，初始化超时时间配置，再根据配置生成默认httpClient对象
    static {

        // Cookie存储
        httpCookieStore = new CustomCookieStore();

        unBuildConfig = RequestConfig.custom();

        // 配置
        final RequestConfig config = unBuildConfig
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setRedirectsEnabled(false)
                // .setProxy(new HttpHost("127.0.0.1", 8866))      // 开发环境设置代理
                .setCircularRedirectsAllowed(true)
                .build();

        // 动态配置
        defaultContext = HttpClientContext.create();
        defaultContext.setCookieStore(httpCookieStore);
        defaultContext.setRequestConfig(config);

    }

    /**
     *    _____/\\\\\\\\\\\\__/\\\\\\\\\\\\\\\__/\\\\\\\\\\\\\\\_
     *     ___/\\\//////////__\/\\\///////////__\///////\\\/////__
     *      __/\\\_____________\/\\\___________________\/\\\_______
     *       _\/\\\____/\\\\\\\_\/\\\\\\\\\\\___________\/\\\_______
     *        _\/\\\___\/////\\\_\/\\\///////____________\/\\\_______
     *         _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     *          _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     *           _\//\\\\\\\\\\\\/__\/\\\\\\\\\\\\\\\_______\/\\\_______
     *            __\////////////____\///////////////________\///________
     *            FROM:http://patorjk.com/software/taag
     */
    public static String doGet(String url) throws IOException, ParseException {
        return HTTP.doGet(url);
    }

    public static String doGet(String url, Map<String, String> params) throws IOException, ParseException {
        return HTTP.doGet(url, params);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public static String doGet(String url, Map<String, String> params, String charset) throws IOException, ParseException {
        return HTTP.doGet(url, params, charset);
    }

    public static String doGet(String url, String charset, Map<String, String> headers) throws IOException, ParseException {
        return HTTP.doGet(url, charset, headers);
    }


    public static CloseableHttpResponse doGet(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset
    ) {
        return HTTP.doGet(url, params, headers, charset);
    }

    /**
     * HTTP Get 获取内容 [主方法]
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param headers 请求头信息
     * @param charset 编码格式
     * @return CloseableHttpResponse
     */
    public static CloseableHttpResponse doGet(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset,
            HttpClientContext context
    ) {
        return HttpUtil2.doGet(url, params, headers, charset, context);
    }

    /***
     *    __/\\\\\\\\\\\\\_________/\\\\\__________/\\\\\\\\\\\____/\\\\\\\\\\\\\\\_
     *     _\/\\\/////////\\\_____/\\\///\\\______/\\\/////////\\\_\///////\\\/////__
     *      _\/\\\_______\/\\\___/\\\/__\///\\\___\//\\\______\///________\/\\\_______
     *       _\/\\\\\\\\\\\\\/___/\\\______\//\\\___\////\\\_______________\/\\\_______
     *        _\/\\\/////////____\/\\\_______\/\\\______\////\\\____________\/\\\_______
     *         _\/\\\_____________\//\\\______/\\\__________\////\\\_________\/\\\_______
     *          _\/\\\______________\///\\\__/\\\_____/\\\______\//\\\________\/\\\_______
     *           _\/\\\________________\///\\\\\/_____\///\\\\\\\\\\\/_________\/\\\_______
     *            _\///___________________\/////_________\///////////___________\///________
     *            FROM:http://patorjk.com/software/taag
     */

    public static String doFilePost(String url, byte[] data) throws IOException {
        return HTTP.doFilePost(url, data);
    }

    public static String doPost(String url, Map<String, String> params, Map<String, String> header)
            throws IOException, ParseException {
        return HTTP.doPost(url, params, header, null);
    }

}

