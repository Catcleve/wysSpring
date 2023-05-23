package com.wys.service;

import com.wys.spring.interfaces.BeanNameAware;
import com.wys.spring.annotation.AutoWired;
import com.wys.spring.annotation.Component;
import com.wys.spring.annotation.Scope;
import com.wys.spring.interfaces.InitializingBean;

@Component
@Scope("singleton")
public class UserService implements BeanNameAware, InitializingBean,UserServiceInterface {

    private String name;

    @AutoWired
    private OrderService orderService;

    @Override
    public void test(){
        System.out.println("原始的test方法");
    }

    @Override
    public void setBeanName(String beanName) {
        name = beanName;
    }

    public String getName() {
        return name;
    }

    /**
     * 这个方法也会被代理
     */
    @Override
    public void afterPropertiesSet() {
        System.out.println("自定义的 afterPropertiesSet方法 beanName = " + name);
    }
}
