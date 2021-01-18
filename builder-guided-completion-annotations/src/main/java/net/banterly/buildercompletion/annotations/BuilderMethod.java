package net.banterly.buildercompletion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuilderMethod {
    enum Type{
        MANDATORY, OPTIONAL
    }

    Type type() default Type.OPTIONAL;
    boolean repeatable() default false;
    String[] incompatibleWith() default "";
}
