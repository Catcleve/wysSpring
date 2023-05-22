package com.wys.service;

import com.wys.spring.WysApplicationContext;

public class Test {

    public static void main(String[] args) {
        WysApplicationContext<ApplicationConfig> applicationContext = new WysApplicationContext<>(ApplicationConfig.class);
        UserService bean = (UserService) applicationContext.getBean("userService1");
        System.out.println("bean = " + bean);
    }

}
