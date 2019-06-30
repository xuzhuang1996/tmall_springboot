package com.tmall.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

//用于捕捉Exception异常以及其子类,捕捉到之后，把异常信息返回
@RestController
@ControllerAdvice//自定义json文档，供特定的控制器返回
public class GloabalExceptionHandler {
    private static Logger logger = LoggerFactory.getLogger(GloabalExceptionHandler.class);
    @ExceptionHandler(value = Exception.class)
    public String defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        logger.error("全局异常类捕获");
        logger.error(e.getMessage(),e);//这样就将所有异常都输出到日志
        e.printStackTrace();
        Class constraintViolationException = Class.forName("org.hibernate.exception.ConstraintViolationException");
        if(null!=e.getCause()  && constraintViolationException==e.getCause().getClass()) {
            return "违反了约束，多半是外键约束";
        }
        return e.getMessage();
    }
}
