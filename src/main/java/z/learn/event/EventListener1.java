package z.learn.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;

public class EventListener1 implements ApplicationListener<ApplicationContextEvent> {

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        System.out.println(event);
    }
}
/**
 *
 * 调用栈
 * onApplicationEvent:10, SpringEventDemo (z.learn.event)
 * onApplicationEvent:6, SpringEventDemo (z.learn.event)
 * doInvokeListener:172, SimpleApplicationEventMulticaster (org.springframework.context.event)
 * invokeListener:165, SimpleApplicationEventMulticaster (org.springframework.context.event)
 * multicastEvent:139, SimpleApplicationEventMulticaster (org.springframework.context.event)
 * publishEvent:393, AbstractApplicationContext (org.springframework.context.support)
 * publishEvent:347, AbstractApplicationContext (org.springframework.context.support)
 * finishRefresh:883, AbstractApplicationContext (org.springframework.context.support)
 * refresh:546, AbstractApplicationContext (org.springframework.context.support)
 * <init>:139, ClassPathXmlApplicationContext (org.springframework.context.support)
 * <init>:83, ClassPathXmlApplicationContext (org.springframework.context.support)
 * main:9, SpringEventMain (z.learn.event)
 */