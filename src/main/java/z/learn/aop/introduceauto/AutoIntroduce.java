package z.learn.aop.introduceauto;


import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import z.learn.aop.SayHello;

public class AutoIntroduce extends DelegatingIntroductionInterceptor implements BeanPostProcessor, IntroductionInterceptor, SayHello {

    public AutoIntroduce() {
        super();
        suppressInterface(BeanPostProcessor.class);
    }

    @Override
    public void sayHello(String msg) {
        System.out.println("fuck.. introduce.. msg=" + msg);
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        if (isMethodOnIntroducedInterface(mi)) {
            // Using the following method rather than direct reflection, we
            // get correct handling of InvocationTargetException
            // if the introduced method throws an exception.
            Object retVal = AopUtils.invokeJoinpointUsingReflection(this, mi.getMethod(), mi.getArguments());

            // Massage return value if possible: if the delegate returned itself,
            // we really want to return the proxy.
            if (retVal == this && mi instanceof ProxyMethodInvocation) {
                Object proxy = ((ProxyMethodInvocation) mi).getProxy();
                if (mi.getMethod().getReturnType().isInstance(proxy)) {
                    retVal = proxy;
                }
            }
            return retVal;
        }

        System.out.println("fucking kid.. introduce.. interceptor");
        return doProceed(mi);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ProxyFactory proxyFactory = new ProxyFactory();

        proxyFactory.setProxyTargetClass(true);
        proxyFactory.setTarget(bean);
        proxyFactory.addAdvice(this);

        return proxyFactory.getProxy();
    }
}
