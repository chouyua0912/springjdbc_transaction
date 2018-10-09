package z.learn.autowire;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import z.learn.TransactionUpdate1;

/**
 * AutowireCapableBeanFactory 用法
 * 编程式初始化bean
 * 可以初始化原始bean，但是不用被代理后的bean
 */
public class AutowireFactoryDemo {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
        AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();

        TransactionUpdate1 update1 = new TransactionUpdate1();

        factory.autowireBean(update1);
        TransactionUpdate1 wrapped = (TransactionUpdate1) factory.initializeBean(update1, "testBeanName");  // 编程式调用 update1是会被调用initializeBean方法
        AopUtils.isAopProxy(update1);       // false
        AopUtils.isAopProxy(wrapped);       // true
    }
}
