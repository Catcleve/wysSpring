package com.wys.spring;

import com.wys.spring.exception.NoBeanNamedException;

import java.io.File;
import java.net.URL;
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

    private final Map<String, Object> singletonBeanMap = new ConcurrentHashMap<>();

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
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

                                Component component = aClass.getAnnotation(Component.class);
                                String beanName = component.value().equals("") ? getBeanDefaultName(aClass) : component.value();

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

                        } catch (ClassNotFoundException e) {
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
        return null;
    }

    private static String getBeanDefaultName(Class<?> aClass) {
        String beanFullName = aClass.getName();
        beanFullName = beanFullName.substring(beanFullName.lastIndexOf(".") + 1);

        return beanFullName.substring(0, 1).toLowerCase() + beanFullName.substring(1);
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

