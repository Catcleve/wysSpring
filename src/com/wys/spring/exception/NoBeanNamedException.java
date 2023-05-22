package com.wys.spring.exception;

public class NoBeanNamedException extends RuntimeException{

    public NoBeanNamedException(String beanName) {
        super("no bean named [" + beanName + "]");
    }
}
