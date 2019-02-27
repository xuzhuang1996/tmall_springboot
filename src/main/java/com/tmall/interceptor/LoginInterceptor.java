package com.tmall.interceptor;

import com.tmall.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginInterceptor implements HandlerInterceptor {

    //Called after HandlerMapping determined an appropriate handler object, but before HandlerAdapter invokes the handler.
    //在HandlerMapping确定一个处理对象之后，HandlerAdapter处理之前，就是在进入controller的方法之前进行的拦截
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws IOException {
        String[] requireAuthPages = new String[]{
                "buy",
                "alipay",
                "payed",
                "cart",
                "bought",
                "confirmPay",
                "orderConfirmed",

                "forebuyone",
                "forebuy",
                "foreaddCart",
                "forecart",
                "forechangeOrderItem",
                "foredeleteOrderItem",
                "forecreateOrder",
                "forepayed",
                "forebought",
                "foreconfirmPay",
                "foreorderConfirmed",
                "foredeleteOrder",
                "forereview",
                "foredoreview"

        };
        HttpSession session = httpServletRequest.getSession();
        String contextPath=session.getServletContext().getContextPath();//        为/tmall_springboot
        String uri = httpServletRequest.getRequestURI();//                        为/tmall_springboot/cart
        uri = StringUtils.remove(uri, contextPath+"/");
        String page = uri;//cart

        if(begingWith(page, requireAuthPages)){
            User user = (User) session.getAttribute("user");
            if(user!=null)return true;
            httpServletResponse.sendRedirect("login");
            return false;
//            Subject subject = SecurityUtils.getSubject();
//            if(!subject.isAuthenticated()) {
//                httpServletResponse.sendRedirect("login");
//                return false;
//            }
        }
        return true;
    }

    private boolean begingWith(String page, String[] requiredAuthPages) {
        boolean result = false;
        for (String requiredAuthPage : requiredAuthPages) {
            if(StringUtils.startsWith(page, requiredAuthPage)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
