package com.wys.spring.interfaces;

/**
 * 初始化bean
 * 自己定义bean依赖注入后的初始化操作
 * @author maonengneng
 * @date 2023/05/23
 */
public interface InitializingBean {

    void afterPropertiesSet();

}
