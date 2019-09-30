package com.imooc.mmall.controller.portal;

import com.imooc.mmall.common.Const;
import com.imooc.mmall.common.RedisPool;
import com.imooc.mmall.common.ResponseCode;
import com.imooc.mmall.common.ServerResponse;
import com.imooc.mmall.pojo.User;
import com.imooc.mmall.service.IUserService;
import com.imooc.mmall.util.CookieUtil;
import com.imooc.mmall.util.JedisUtil;
import com.imooc.mmall.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by geely
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private IUserService iUserService;


    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username,
                                      String password,
                                      HttpServletRequest request,
                                      HttpServletResponse httpServletResponse,
                                      HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            jedisUtil.setex(session.getId(), Const.RedisCacheExtime.REDIS_SESSION_EXTIME, JsonUtil.obj2String(response.getData()));
        }
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }


    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }


    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        String userJsonStr = jedisUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        return ServerResponse.createBySuccess(user);
    }


    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }


    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }


    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }


    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest request, String passwordOld, String passwordNew) {
        String loginToken = CookieUtil.readLoginToken(request);
        String userJsonStr = jedisUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }


    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpServletRequest request, User user) {
        String loginToken = CookieUtil.readLoginToken(request);
        String userJsonStr = jedisUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);

        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
            String userStr = JsonUtil.obj2String(user);
            jedisUtil.setex(loginToken, userStr, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        String userJsonStr = jedisUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);

        return iUserService.getInformation(currentUser.getId());
    }


}
