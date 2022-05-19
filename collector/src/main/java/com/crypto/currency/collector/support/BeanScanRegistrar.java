package com.crypto.currency.collector.support;

import com.crypto.currency.collector.support.annotation.FunctionalScan;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Panzi
 * @Description register class path by FunctionalScan
 * @date 2022/5/15 22:03
 */
@Slf4j
public class BeanScanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;  //todo maybe not need?

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
        BeanDefinitionRegistry beanDefinitionRegistry) {

        log.info("start scan  worker Registrar");
        Stopwatch started = Stopwatch.createStarted();
        AnnotationAttributes annoAttrs =
            AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(FunctionalScan.class.getName()));
        String[] basePackages = annoAttrs.getStringArray("basePackages");
        if (basePackages == null || basePackages.length == 0) {
            throw new RuntimeException("BeanScan basePackages is empty.");
        }
        BeanScanHandler scanHandle = new BeanScanHandler(beanDefinitionRegistry);
        scanHandle.doScan(basePackages);
        log.info("end scan worker");
        started.stop();

    }
}
