package com.tmall.anno;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.METHOD}) //声明应用在类上
@Retention(RetentionPolicy.RUNTIME) //运行期生效
@Documented //Java Doc
@Component
public @interface myAuth {
}
