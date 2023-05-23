package com.wys.spring.interfaces;

/**
 * bean处理器
 *
 * @author maonengneng
 * @date 2023/05/23
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);


    Object postProcessAfterInitialization(Object bean, String beanName);

}
