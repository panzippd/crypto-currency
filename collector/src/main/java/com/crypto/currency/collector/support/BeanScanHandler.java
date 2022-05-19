package com.crypto.currency.collector.support;

import com.crypto.currency.collector.support.annotation.Crypto;
import com.crypto.currency.collector.support.annotation.Exchange;
import com.crypto.currency.common.exception.BusinessException;
import com.crypto.currency.common.utils.CollectionUtils;
import com.crypto.currency.common.utils.ObjectUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Map;
import java.util.Set;

/**
 * @author Panzi
 * @Description update beanName and register again
 * @date 2022/5/15 21:58
 */
@Slf4j
public class BeanScanHandler extends ClassPathBeanDefinitionScanner {

    private final String cryptoClassName = Crypto.class.getName();
    private final String exchangeClassName = Exchange.class.getName();
    private static final String ID = "id";

    public static final String CRYPTO = "crypto_";
    public static final String EXCHANGE = "exchange_";
    public static final String EXCHANGE_NAME = "exchangeName";
    public static final String EXCHANGE_ID = "exchangeId";
    public static final String NAME = "name";
    public static final String SYMBOL = "symbol";

    public BeanScanHandler(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public void registerDefaultFilters() {
        addIncludeFilter(new AnnotationTypeFilter(Exchange.class));
        addIncludeFilter(new AnnotationTypeFilter(Crypto.class));
    }

    @SneakyThrows
    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {

        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (CollectionUtils.isNotEmpty(beanDefinitionHolders)) {
            processBeanDefinitions(beanDefinitionHolders);
        }
        log.info("##########-Total exchange and crypto : " + beanDefinitionHolders.size() + "-###########");
        return beanDefinitionHolders;
    }

    /**
     * process definitions
     *
     * @param beanDefinitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {

        ScannedGenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = ((ScannedGenericBeanDefinition)holder.getBeanDefinition());
            if (definition == null) {
                continue;
            }
            AnnotationMetadata annotationMetadata = definition.getMetadata();
            Map<String, Object> attributeMap = annotationMetadata.getAnnotationAttributes(exchangeClassName);
            if (CollectionUtils.isNotEmpty(attributeMap)) {
                applyExchangeAnnotation(definition, holder, attributeMap);
                continue;
            }
            attributeMap = annotationMetadata.getAnnotationAttributes(cryptoClassName);
            if (CollectionUtils.isNotEmpty(attributeMap)) {
                applyCryptoAnnotation(definition, holder, attributeMap);
            }
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {

        return super.isCandidateComponent(beanDefinition) && (
            beanDefinition.getMetadata().hasAnnotation(Exchange.class.getName()) || beanDefinition.getMetadata()
                .hasAnnotation(Crypto.class.getName()));
    }

    /**
     * crypto annotation inject
     *
     * @param definition
     * @param holder
     * @param attributeMap
     */
    private void applyCryptoAnnotation(ScannedGenericBeanDefinition definition, BeanDefinitionHolder holder,
        Map<String, Object> attributeMap) {

        String id = ObjectUtils.toString(attributeMap.get(ID));
        String symbol = ObjectUtils.toString(attributeMap.get(SYMBOL));
        if (StringUtils.isAnyBlank(symbol, id)) {
            BusinessException.throwIfMessage(String.format("The id and symbol of the %s @Crypto Bean is emtpy.",
                holder.getBeanDefinition().getBeanClassName()));
        }
        definition.getPropertyValues().add(ID, id).add(SYMBOL, symbol);
        log.info("Crypto,id:{},symbol:{} ,className:{} scan succeed.", id, symbol,
            holder.getBeanDefinition().getBeanClassName());
        this.getRegistry().removeBeanDefinition(holder.getBeanName());
        String beanName = CRYPTO + id;
        if (this.getRegistry().containsBeanDefinition(beanName)) {
            BusinessException.throwIfMessage(
                String.format("%s,The %s bean already exists.", holder.getBeanDefinition().getBeanClassName(),
                    beanName));
        }
        this.getRegistry().registerBeanDefinition(beanName, definition);
    }

    /**
     * exchange annotation inject
     *
     * @param definition
     * @param holder
     * @param attributeMap
     */
    private void applyExchangeAnnotation(ScannedGenericBeanDefinition definition, BeanDefinitionHolder holder,
        Map<String, Object> attributeMap) {

        String id = ObjectUtils.toString(attributeMap.get(ID));
        String exchangeName = ObjectUtils.toString(attributeMap.get(NAME));
        if (StringUtils.isAnyBlank(exchangeName, id)) {
            BusinessException.throwIfMessage(String.format("The id and name of the %s @Exchange Bean is emtpy.",
                holder.getBeanDefinition().getBeanClassName()));
        }
        definition.getPropertyValues().add(EXCHANGE_ID, id).add(EXCHANGE_NAME, exchangeName);
        log.info("Exchange,id:{},name:{} ,className:{} scan succeed.", id, exchangeName,
            holder.getBeanDefinition().getBeanClassName());
        this.getRegistry().removeBeanDefinition(holder.getBeanName());
        String beanName = EXCHANGE + id;
        if (this.getRegistry().containsBeanDefinition(beanName)) {
            BusinessException.throwIfMessage(
                String.format("%s,The %s bean already exists.", holder.getBeanDefinition().getBeanClassName(),
                    beanName));
        }
        this.getRegistry().registerBeanDefinition(beanName, definition);
    }

}
