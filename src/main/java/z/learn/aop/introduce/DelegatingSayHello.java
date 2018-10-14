package z.learn.aop.introduce;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import z.learn.aop.SayHello;

public class DelegatingSayHello extends DelegatingIntroductionInterceptor implements SayHello {

    @Override
    public void sayHello(String msg) {
        System.out.println("Introduced.. msg=" + msg);
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {        // 对于所有方法还是会拦截
        System.out.println("----intercepted invoke");
        return super.invoke(mi);
    }
}
