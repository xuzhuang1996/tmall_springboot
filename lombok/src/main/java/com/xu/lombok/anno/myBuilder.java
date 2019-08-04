package com.xu.lombok.anno;

import java.lang.annotation.*;

//该注解主要是为了增强被修饰的类：Builder建筑者模式
//ElementType.TYPE用于描述类、接口(包括注解类型) 或enum声明
@Target({ ElementType.TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface myBuilder {
}
