package com.crypto.currency.collector.support.annotation;

import com.crypto.currency.collector.support.BeanScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/15 21:58
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(BeanScanRegistrar.class)
@Documented
public @interface FunctionalScan {

    String[] basePackages() default {};

}
