package com.xu.lombok.anno;

import java.lang.annotation.*;

/**
 * 实现Log注解，每个被注解的类，都自动生成一个Log的日志对象。免得自己写对象
 */
@Target({ ElementType.TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
}
