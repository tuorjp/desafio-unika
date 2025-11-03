package jp.tuor.backend.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Obrigatorio {
    boolean isObrigatorio() default true;
    String dependeDeCampo() default "";
}
