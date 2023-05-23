package com.wys.spring;

import com.wys.spring.annotation.AutoWired;
import com.wys.spring.annotation.Component;
import com.wys.spring.annotation.ComponentScan;
import com.wys.spring.annotation.Scope;
import com.wys.spring.exception.NoBeanNamedException;
import com.wys.spring.interfaces.BeanNameAware;
import com.wys.spring.interfaces.BeanPostProcessor;
import com.wys.spring.interfaces.InitializingBean;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * spring 上下文类
 *
 * @author maonengneng
 * @date 2023/05/22
 */
public class WysApplicationContext<T> {

    /**
     * 单例bean池
     */
    private final Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>();

    /**
     * bean定义池
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final ArrayList<BeanPostProcessor> BeanPostProcessorList = new ArrayList<>();
    private Class<T> tClass;


    public WysApplicationContext(Class<T> tClass) {
        this.tClass = tClass;
        //扫描bean

        //1 先找到 componentScan注解
        if (tClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan annotation = tClass.getAnnotation(ComponentScan.class);
            // 扫描路径com.wys.service
            String value = annotation.value();
            //转换成目录格式
            value = value.replace(".", "/");
            ClassLoader classLoader = this.getClass().getClassLoader();
            // file:/D:/code/learn/spring/wysSpring/out/production/wysSpring/com/wys/service
            URL resource = classLoader.getResource(value);
            assert resource != null;
            // 获取到了编译过后的class文件的目录
            // /D:/code/learn/spring/wysSpring/out/production/wysSpring/com/wys/service
            String file = resource.getFile();
            File dir = new File(file);
            //检测是否是目录
            if (dir.isDirectory()) {
                for (File listFile : Objects.requireNonNull(dir.listFiles())) {
                    //获取以 .class结尾的文件
                    //D:\code\learn\spring\wysSpring\out\production\wysSpring\com\wys\service\ApplicationConfig.class
                    String name = listFile.getAbsolutePath();
                    if (name.endsWith(".class")) {
                        //替换为 com.wys.service.ApplicationConfig格式来获取注解
                        name = name.substring(name.lastIndexOf("com"), name.lastIndexOf(".class")).replace("\\", ".");
                        try {
                            Class<?> aClass = classLoader.loadClass(name);
                            if (aClass.isAnnotationPresent(Component.class)) {

                                //查看是否为BeanPostProcessor,如果是的话加入处理流程集合
                                if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                                    BeanPostProcessor postProcesses = (BeanPostProcessor) aClass.getConstructor().newInstance();
                                    BeanPostProcessorList.add(postProcesses);
                                }


                                Component component = aClass.getAnnotation(Component.class);
                                //java.bean包中带的方法,获取默认类名
                                String beanName = component.value().equals("") ?
                                        Introspector.decapitalize(aClass.getSimpleName()) : component.value();

                                //获取生命周期
                                String scope = "singleton";
                                if (aClass.isAnnotationPresent(Scope.class)) {
                                    scope = aClass.getAnnotation(Scope.class).value();
                                }
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
                                beanDefinition.setScope(scope);
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }

                        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                                 IllegalAccessException | NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        //扫描完成 创建bean
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            //单例 创建然后放入单例池
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonBeanMap.put(beanName, bean);
            }
        });


    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {

        try {
            Class<?> type = beanDefinition.getType();
            Object instance = type.getConstructor().newInstance();
            //简单依赖注入
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(AutoWired.class)) {
                    field.setAccessible(true);
                    String name = field.getName();
                    Object fieldBean = getBean(name);
                    field.set(instance, fieldBean);
                }
            }

            //spring感知接口
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            //bean初始化前处理
            for (BeanPostProcessor postProcesses : BeanPostProcessorList) {
                Object o = postProcesses.postProcessBeforeInitialization(instance, beanName);
                if (o != null) {
                    return o;
                }
            }

            //自定义初始化调用
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            //bean初始化后处理
            for (BeanPostProcessor postProcesses : BeanPostProcessorList) {
                instance = postProcesses.postProcessAfterInitialization(instance, beanName);
            }


            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {

    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (Objects.isNull(beanDefinition)) {
            throw new NoBeanNamedException(beanName);
        }
        if (beanDefinition.getScope().equals("singleton")) {
            Object o = singletonBeanMap.get(beanName);
            if (Objects.isNull(o)) {
                o = createBean(beanName, beanDefinition);
                singletonBeanMap.put(beanName, o);
            }
            return o;
        } else {
            return createBean(beanName, beanDefinition);
        }
    }
}

