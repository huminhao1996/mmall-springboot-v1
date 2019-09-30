package com.imooc.mmall.aop;

import com.imooc.mmall.common.Const;
import com.imooc.mmall.common.RedisPool;
import com.imooc.mmall.common.ServerResponse;
import com.imooc.mmall.pojo.User;
import com.imooc.mmall.util.CookieUtil;
import com.imooc.mmall.util.JedisUtil;
import com.imooc.mmall.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @描述:
 * @创建人: hmh
 * @创建时间: 2019/9/30 14:32
 * @公司: 杭州中车数字科技有限公司
 */
@Component
@Slf4j
@org.aspectj.lang.annotation.Aspect
public class Aspect {

    @Autowired
    private JedisUtil jedisUtil;


    //execution表达式自行搜索引擎
    @Pointcut("execution(public * com.imooc.mmall.*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public ServerResponse printParam(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取请求的方法
        Signature sig = joinPoint.getSignature();
        String method = joinPoint.getTarget().getClass().getName() + "." + sig.getName();
        //获取请求的参数
        Object[] args = joinPoint.getArgs();
        //fastjson转换
        String params = JsonUtil.obj2String(args);
        //打印请求参数
        log.info("开始执行方法,方法名:{},参数:{}", method, params);

        //登录判断
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken))
            return ServerResponse.createByErrorMessage("用户未登录");
        String json = jedisUtil.get(loginToken);
        User user = JsonUtil.string2Obj(json, User.class);
        if (user == null)
            return ServerResponse.createByErrorMessage("查询不到该用户");
        //如果user不为空，则重置session的时间，即调用expire命令
        jedisUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        return (ServerResponse) joinPoint.proceed();
    }
}
