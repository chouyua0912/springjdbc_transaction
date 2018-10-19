package z.learn.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取类型参数的方法
 * 进行运行时分析
 */
public class SpringEventMain {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-event.xml");
        EventListener1 demo = context.getBean(EventListener1.class);

        printGenericTypeInfo(demo);
        printGenericTypeInfo(context.getBean(EventListener2.class));
    }

    private static void printGenericTypeInfo(Object param) {
        System.out.println(param);
        Type[] types = param.getClass().getGenericInterfaces();

        Type[] arguments = ((ParameterizedType) (types[0])).getActualTypeArguments();
        System.out.println(arguments[0].getTypeName());
        System.out.println(arguments[0]);
        System.out.println(arguments[0] == ApplicationContextEvent.class);
    }
}
