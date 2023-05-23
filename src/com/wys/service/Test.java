package com.wys.service;

import com.wys.spring.WysApplicationContext;

public class Test {

    public static void main(String[] args) {
        WysApplicationContext<ApplicationConfig> applicationContext = new WysApplicationContext<>(ApplicationConfig.class);
        UserServiceInterface bean = (UserServiceInterface) applicationContext.getBean("userService");
        System.out.println("bean = " + bean);
        bean.test();
    }

}
