package com.imooc.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @描述:
 * @创建人: hmh
 * @创建时间: 2019/9/30 10:18
 * @公司: 杭州中车数字科技有限公司
 */
@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMAIN = ".imooc01.com";
    private final static String COOKIE_NAME = "token";

    /** 
     * 读cookie
     * @param request 
     * @返回值: java.lang.String
     * @创建人: hmh
     * @创建时间: 2019/9/30 10:24
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                log.info("read cookieName:{},cookieValue:{}", ck.getName(), ck.getValue());
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    log.info("return cookieName:{},cookieValue:{}", ck.getName(), ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    /** 
     * 写cookie
     * @param response
     * @param token 
     * @返回值: void
     * @创建人: hmh
     * @创建时间: 2019/9/30 10:24
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        //域名
        cookie.setDomain(COOKIE_DOMAIN);
        //设置在根目录
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        //单位是秒。
        //如果这个maxage不设置的话，cookie就不会写入硬盘，而是写在内存。只在当前页面有效。
        cookie.setMaxAge(60 * 60 * 24 * 365);//如果是-1，代表永久
        log.info("write cookieName:{},cookieValue:{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    /**
     * 删除cookie
     * @param request
     * @param response
     * @返回值: void
     * @创建人: hmh
     * @创建时间: 2019/9/30 10:25
     */
    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck : cks){
                if(StringUtils.equals(ck.getName(),COOKIE_NAME)){
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    //设置成0，代表删除此cookie。
                    ck.setMaxAge(0);
                    log.info("del cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
