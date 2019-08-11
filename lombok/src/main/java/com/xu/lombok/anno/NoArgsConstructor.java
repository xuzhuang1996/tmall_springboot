package com.xu.lombok.anno;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface NoArgsConstructor {
}
