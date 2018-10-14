package z.learn.aop.introduceauto;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import z.learn.aop.BizClass;
import z.learn.aop.SayHello;

public class AutoIntroduceDemo {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("introduce2.xml");
        BizClass bizClass = (BizClass) context.getBean("bizClass");

        bizClass.doBiz("hello guess..");
        System.out.println("------------------------------------------------------------------------------------------------------");
        ((SayHello) bizClass).sayHello("check if introduced biz");
        System.out.println("------------------------------------------------------------------------------------------------------");
    }
}
