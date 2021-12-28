package org.runrun.utils.HTTP;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.RedirectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 网络请求工具类
 *
 * @author jiyec
 */
@Slf4j
public class HttpUtil2 {

    private final BasicCookieStore httpCookieStore = new BasicCookieStore();

    private final HttpClientContext localContext = HttpClientContext.create();
    private final RequestConfig.Builder unBuildConfig;
    private static final CloseableHttpClient httpClient;
    private static final String CHARSET = "UTF-8";

    // 采用静态代码块，初始化部分配置，再根据配置生成默认httpClient对象
    static {

        // SSL准备
        SSLContext sslContext;
        SSLConnectionSocketFactory sslCSF = null;
        try {
            // 信任所有
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            sslCSF = new SSLConnectionSocketFactory(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        // 连接池管理
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslCSF)
                .build();


        // 创建客户端
        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setUserAgent("okhttp/3.12.0")
                .build();
    }

    {
        // 默认配置
        unBuildConfig = RequestConfig.custom().setConnectTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout(Timeout.ofSeconds(5))
//                 .setProxy(new HttpHost("192.168.1.232", 8888))
                .setCircularRedirectsAllowed(true);
        localContext.setCookieStore(httpCookieStore);
    }

    public HttpUtil2() {
        // 默认配置
        localContext.setRequestConfig(unBuildConfig.build());
    }

    /**
     * 自定义配置
     *
     * @param config
     */
    public HttpUtil2(Map<String, Object> config) {
        // 自定义配置
        if (config.containsKey("redirection")) {
            unBuildConfig.setMaxRedirects((int) config.get("redirection"));
            unBuildConfig.setRedirectsEnabled((int) config.get("redirection") != 0);
        }
        localContext.setRequestConfig(unBuildConfig.build());

    }

    /**
     * 获取Cookie
     *
     * @return Map<String, String>
     */
    public Map<String, String> getCookie() {
        List<Cookie> cookiesCustom = httpCookieStore.getCookies();
        Map<String, String> cookies = new HashMap<>();
        for (Cookie cookie : cookiesCustom) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    /**
     * _____/\\\\\\\\\\\\__/\\\\\\\\\\\\\\\__/\\\\\\\\\\\\\\\_
     * ___/\\\//////////__\/\\\///////////__\///////\\\/////__
     * __/\\\_____________\/\\\___________________\/\\\_______
     * _\/\\\____/\\\\\\\_\/\\\\\\\\\\\___________\/\\\_______
     * _\/\\\___\/////\\\_\/\\\///////____________\/\\\_______
     * _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     * _\/\\\_______\/\\\_\/\\\___________________\/\\\_______
     * _\//\\\\\\\\\\\\/__\/\\\\\\\\\\\\\\\_______\/\\\_______
     * __\////////////____\///////////////________\///________
     * FROM:http://patorjk.com/software/taag
     *
     * @param url 发送get请求的url
     * @return String 响应体
     */
    public String doGet(String url) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, null, CHARSET, localContext)), CHARSET);
    }

    public String doGet(String url, Map<String, String> params) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, params, null, CHARSET, localContext)), CHARSET);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     */
    public String doGet(String url, Map<String, String> params, String charset) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, params, null, charset, localContext)), charset);
    }

    public String doGet(String url, String charset, Map<String, String> headers) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, headers, charset, localContext)), charset);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param headers 自定义的请求头信息
     * @return 页面内容
     */
    public String doGet2(String url, Map<String, String> headers) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doGet(url, null, headers, null, localContext)), CHARSET);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url 请求的url地址 ?之前的地址
     * @return 页面内容
     */
    public HttpUtilEntity doGetEntity(String url) throws IOException, ParseException {
        return doGetEntity(url, null, null, CHARSET);
    }

    /**
     * HTTP Get 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param headers 自定义的请求头信息
     * @return 页面内容
     */
    public HttpUtilEntity doGetEntity(String url, Map<String, String> headers) throws IOException, ParseException {
        return doGetEntity(url, null, headers, CHARSET);
    }

    public HttpUtilEntity doGetEntity(String url, Map<String, String> headers, String charset) throws IOException, ParseException {
        return doGetEntity(url, null, headers, charset);
    }

    /**
     * HTTP GET 请求 [实体方法]
     *
     * @param url     待请求链接
     * @param params  链接参数
     * @param headers 自定义请求头
     * @param charset 请求编码
     * @return HttpUtilEntity
     */
    public HttpUtilEntity doGetEntity(String url, Map<String, String> params, Map<String, String> headers, String charset) throws IOException, ParseException {
        CloseableHttpResponse closeableHttpResponse = doGet(url, params, headers, charset, localContext);

        if (null == closeableHttpResponse) return null;

        HttpUtilEntity httpUtilEntity = response2entity(closeableHttpResponse, charset);
        closeableHttpResponse.close();

        return httpUtilEntity;
    }

    public byte[] getContent(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset) throws IOException {
        CloseableHttpResponse httpResponse = doGet(url, params, headers, charset);
        HttpEntity entity = httpResponse.getEntity();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        InputStream inputStream = entity.getContent();
        int len = -1;
        while (-1 != (len = inputStream.read(chunk))) {
            outputStream.write(chunk, 0, len);
        }
        return outputStream.toByteArray();
    }

    public CloseableHttpResponse doGet(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset
    ) {
        return doGet(url, params, headers, charset, localContext);
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
        if (StringUtils.isBlank(url)) {
            return null;
        }
        try {
            UrlEncodedFormEntity urlEncodedFormEntity = genFormEntity(params, charset);

            // 将请求参数和url进行拼接
            if (null != urlEncodedFormEntity)
                url += "?" + EntityUtils.toString(urlEncodedFormEntity);

            HttpGet httpGet = new HttpGet(url);
            addHeader(httpGet, headers);

            return httpClient.execute(httpGet, context);
        } catch (Exception e) {
            if (e.getCause() instanceof RedirectException) {
                // 处理"达到最大重定向异常"
                return (CloseableHttpResponse) context.getResponse();
            }
            e.printStackTrace();
        }
        return null;
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
    public String doPost(String url, Map<String, String> params) throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, null, CHARSET, localContext)), CHARSET);
    }
    public byte[] doPostJson2Byte(String url, String s) throws IOException {
        return doPostJson2Byte(url, null, s);
    }
    public byte[] doPostJson2Byte(String url, Map<String, String> headers, String s) throws IOException {
        CloseableHttpResponse closeableHttpResponse = this.doPostJson2CHR(url, headers, s);
        InputStream content = closeableHttpResponse.getEntity().getContent();
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int len;
        while (-1 != (len = content.read(chunk))){
            arrayOutputStream.write(chunk, 0, len);
        }
        return arrayOutputStream.toByteArray();
    }
    public CloseableHttpResponse doPostJson2CHR(String url, Map<String, String> headers, String body) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if(headers != null)
            headers.forEach(httpPost::setHeader);
        HttpEntity entity = EntityBuilder.create().setText(body).build();
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json");
        return doPost(httpPost, localContext);
    }
    public CloseableHttpResponse doStreamPost(String url, byte[] data) throws IOException {
        InputStreamEntity inputStreamEntity = genStreamEntity(data);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(inputStreamEntity);

        return doPost(httpPost, localContext);
    }

    public String doFilePost(String url, byte[] data) throws IOException {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("captcha", data, ContentType.DEFAULT_BINARY, URLEncoder.encode("captcha.jpg", "utf-8"));
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(multipartEntityBuilder.build());

        try (CloseableHttpResponse response = httpClient.execute(httpPost, localContext)) {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.abort();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (HttpHostConnectException e) {
            throw new RuntimeException("Connect to host failed!", e);
        } catch (ConnectException e) {
            throw new RuntimeException("Connection failed!", e);
        }
        return null;
    }

    /**
     * HTTP Post 获取内容
     *
     * @param url     请求的url地址 ?之前的地址
     * @param params  请求的参数
     * @param charset 编码格式
     * @return 页面内容
     * @throws IOException IO异常
     */
    public String doPost(String url, Map<String, String> params, String charset)
            throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, null, charset, localContext)), charset);
    }

    public String doPost(String url, Map<String, String> params, Map<String, String> headers, String charset)
            throws IOException, ParseException {
        return getString(Objects.requireNonNull(doPost(url, params, headers, charset, localContext)), charset);
    }

    public HttpUtilEntity doPostEntity(String url, Map<String, String> params) throws IOException, ParseException {
        return doPostEntity(url, params, null, "UTF-8");
    }

    public HttpUtilEntity doPostEntity(String url, Map<String, String> params, Map<String, String> headers) throws IOException, ParseException {
        return doPostEntity(url, params, headers, "UTF-8");
    }

    public HttpUtilEntity doPostEntity(String url, Map<String, String> params, String charset) throws IOException, ParseException {
        return doPostEntity(url, params, null, charset);
    }

    public HttpUtilEntity doPostEntity(String url, Map<String, String> params, Map<String, String> headers, String charset) throws IOException, ParseException {
        CloseableHttpResponse closeableHttpResponse = doPost(url, params, headers, charset, localContext);
        if (null == closeableHttpResponse) return null;
        HttpUtilEntity httpUtilEntity = response2entity(closeableHttpResponse, charset);
        closeableHttpResponse.close();
        return httpUtilEntity;
    }

    public static CloseableHttpResponse doPost(
            String url,
            Map<String, String> params,
            Map<String, String> headers,
            String charset,
            HttpClientContext context
    ) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        HttpPost httpPost = new HttpPost(url);
        // 添加请求头
        addHeader(httpPost, headers);

        // 生成、添加请求体
        UrlEncodedFormEntity urlEncodedFormEntity = genFormEntity(params, charset);
        if (null != urlEncodedFormEntity) {
            httpPost.setEntity(urlEncodedFormEntity);
        }

        return doPost(httpPost, context);
    }

    public static CloseableHttpResponse doPost(
            HttpPost httpPost,
            HttpClientContext context
    ) {

        try {
            return httpClient.execute(httpPost, context);
        } catch (IOException e) {
            if (e.getCause() instanceof RedirectException) {
                // 处理"达到最大重定向异常"
                return (CloseableHttpResponse) context.getResponse();
            }
            e.printStackTrace();
        }
        return null;
    }

    /***
     *    __/\\\________/\\\_____/\\\\\\\\\_____/\\\\\_____/\\\__/\\\\\\\\\\\\_____/\\\______________/\\\\\\\\\\\\\\\_
     *     _\/\\\_______\/\\\___/\\\\\\\\\\\\\__\/\\\\\\___\/\\\_\/\\\////////\\\__\/\\\_____________\/\\\///////////__
     *      _\/\\\_______\/\\\__/\\\/////////\\\_\/\\\/\\\__\/\\\_\/\\\______\//\\\_\/\\\_____________\/\\\_____________
     *       _\/\\\\\\\\\\\\\\\_\/\\\_______\/\\\_\/\\\//\\\_\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\\\\\\\\\_____
     *        _\/\\\/////////\\\_\/\\\\\\\\\\\\\\\_\/\\\\//\\\\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\///////______
     *         _\/\\\_______\/\\\_\/\\\/////////\\\_\/\\\_\//\\\/\\\_\/\\\_______\/\\\_\/\\\_____________\/\\\_____________
     *          _\/\\\_______\/\\\_\/\\\_______\/\\\_\/\\\__\//\\\\\\_\/\\\_______/\\\__\/\\\_____________\/\\\_____________
     *           _\/\\\_______\/\\\_\/\\\_______\/\\\_\/\\\___\//\\\\\_\/\\\\\\\\\\\\/___\/\\\\\\\\\\\\\\\_\/\\\\\\\\\\\\\\\_
     *            _\///________\///__\///________\///__\///_____\/////__\////////////_____\///////////////__\///////////////__
     *
     */
    private HttpUtilEntity response2entity(CloseableHttpResponse closeableHttpResponse, String charset) throws IOException, ParseException {

        // 处理头信息
        Header[] allHeaders = closeableHttpResponse.getHeaders();
        Map<String, String> allHeaderMap = new HashMap<>();

        for (Header header : allHeaders) {
            // if(allHeaderMap.containsKey(header.getName()))
            //     allHeaderMap.put(header.getName(), allHeaderMap.get(header.getName()) + ";" + header.getValue());
            // else
            allHeaderMap.put(header.getName(), header.getValue());
        }

        HttpUtilEntity httpUtilEntity = new HttpUtilEntity();
        // 状态码
        httpUtilEntity.setStatusCode(closeableHttpResponse.getCode());
        httpUtilEntity.setHeaders(allHeaderMap);
        HttpEntity entity = closeableHttpResponse.getEntity();
        httpUtilEntity.setBody(EntityUtils.toString(entity, charset));
        httpUtilEntity.setCookies(getCookie());
        return httpUtilEntity;
    }

    private static InputStreamEntity genStreamEntity(byte[] data) {
        return new InputStreamEntity(new ByteArrayInputStream(data), ContentType.MULTIPART_FORM_DATA);
    }

    private static UrlEncodedFormEntity genFormEntity(Map<String, String> params, String charset) {
        if (null == params || params.isEmpty()) return null;

        List<NameValuePair> pairs;

        pairs = new ArrayList<>(params.size());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value;
            Object obj = entry.getValue();
            if (obj instanceof Integer) {
                value = Integer.toString((int) obj);
            } else {
                value = entry.getValue();
            }
            if (value != null) {
                pairs.add(new BasicNameValuePair(entry.getKey(), value));
            }
        }
        return new UrlEncodedFormEntity(pairs, Charset.forName(charset));
    }

    private static void addHeader(HttpUriRequestBase http, Map<String, String> headers) {
        if (null != headers) headers.forEach(http::setHeader);
    }

    private static String getString(CloseableHttpResponse response, String charset) throws IOException, ParseException {

        HttpEntity entity = response.getEntity();

        String result = null;
        if (entity != null) {
            result = EntityUtils.toString(entity, charset);
        }
        EntityUtils.consume(entity);
        response.close();
        return result;
    }

}

