package com.bjpowernode.p2p.interceptor;

import com.bjpowernode.p2p.common.constant.Constants;
import com.bjpowernode.p2p.model.user.User;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName:UserInterceptor
 * Package:com.bjpowernode.p2p.interceptor
 * Description:
 *
 * @date:2019/10/31 10:38
 * @author:guoxin
 */
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //设置拦截的规则
        User sessionUser = (User) request.getSession().getAttribute(Constants.SESSION_USER);

        //判断用户
        if (!ObjectUtils.allNotNull(sessionUser)) {

            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
