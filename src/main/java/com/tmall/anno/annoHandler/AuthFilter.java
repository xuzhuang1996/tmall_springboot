package com.tmall.anno.annoHandler;

import com.tmall.anno.myAuth;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//定义好后，然后在往容器添加该拦截器
public class AuthFilter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        myAuth auth = getAuth(o);
        if(auth != null) {
            //说明该方法上有拦截注解，需要进行验证
            System.out.println("拦截了一下");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    //preHandle中
    private myAuth getAuth(Object handler) {
        if(!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getMethod().getAnnotation(myAuth.class);
    }
}
