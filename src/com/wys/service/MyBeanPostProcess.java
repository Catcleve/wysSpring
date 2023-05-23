package com.wys.service;

import com.wys.spring.annotation.Component;
import com.wys.spring.interfaces.BeanPostProcessor;

import java.lang.reflect.Proxy;

@Component
public class MyBeanPostProcess implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("postProcessBefore方法执行");
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        //System.out.println("bean = " + bean + ", beanName = " + beanName);
        System.out.println("postProcessAfter方法执行");
        //模拟aop、
        if (beanName.equals("userService")) {
            return Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                if (method.getName().equals("test")) {
                    System.out.println("aop处理逻辑");
                }
                return method.invoke(bean, args);
            });
        }
        return bean;
    }
}
