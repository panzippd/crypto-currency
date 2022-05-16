package com.crypto.currency.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Panzi
 * @Description Utils for spring bean
 * @date 2022/5/16 15:40
 */
@Component
public class SpringBeanUtils extends ApplicationObjectSupport {

    private static volatile ApplicationContext context;

    @Override
    protected void initApplicationContext(ApplicationContext context) throws BeansException {
        super.initApplicationContext(context);
        SpringBeanUtils.context = context;
    }

    /**
     * get system env
     *
     * @return
     */
    public static String[] getCurrentEnv() {

        if (context != null) {
            return context.getEnvironment().getActiveProfiles();
        }
        return new String[0];
    }

    /**
     * get Bean by name
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return context.getBean(name);
    }

    /**
     * @param annotationType
     * @return
     */
    public static Map<String, Object> getAnnotations(Class<? extends Annotation> annotationType) {

        return context.getBeansWithAnnotation(annotationType);
    }

    /**
     * get Bean by class
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * get Bean by name and class
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

}
