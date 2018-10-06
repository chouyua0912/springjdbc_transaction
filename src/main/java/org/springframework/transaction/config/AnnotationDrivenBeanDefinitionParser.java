/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.config;

import org.w3c.dom.Element;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser
 * BeanDefinitionParser} implementation that allows users to easily configure       注册为internalTransactionAdvisor
 * all the infrastructure beans required to enable annotation-driven transaction
 * demarcation.
 *
 * <p>By default, all proxies are created as JDK proxies. This may cause some       默认都是用JDK的动态代理
 * problems if you are injecting objects as concrete classes rather than            编程式注册了4个bean
 * interfaces. To overcome this restriction you can set the                         TransactionalEventListenerFactory
 * '{@code proxy-target-class}' attribute to '{@code true}', which                  AnnotationTransactionAttributeSource
 * will result in class-based proxies being created.                                TransactionInterceptor
 *                                                                                  BeanFactoryTransactionAttributeSourceAdvisor
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Stephane Nicoll
 * @since 2.0
 */
class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

    /**
     * Parses the {@code <tx:annotation-driven/>} tag. Will                 处理注解式事务
     * {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary register an AutoProxyCreator}
     * with the container as necessary.
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        registerTransactionalEventListenerFactory(parserContext);
        String mode = element.getAttribute("mode");
        if ("aspectj".equals(mode)) {                           // 没有设置mode的话，默认使用JDK动态代理
            // mode="aspectj"
            registerTransactionAspect(element, parserContext);
        }
        else {
            // mode="proxy"
            AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);   // 默认情况的JDK代理
        }
        return null;
    }

    private void registerTransactionAspect(Element element, ParserContext parserContext) {          // AspectJ模式的代理
        String txAspectBeanName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME;
        String txAspectClassName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_CLASS_NAME;
        if (!parserContext.getRegistry().containsBeanDefinition(txAspectBeanName)) {
            RootBeanDefinition def = new RootBeanDefinition();
            def.setBeanClassName(txAspectClassName);
            def.setFactoryMethodName("aspectOf");
            registerTransactionManager(element, def);
            parserContext.registerBeanComponent(new BeanComponentDefinition(def, txAspectBeanName));
        }
    }

    private static void registerTransactionManager(Element element, BeanDefinition def) {
        def.getPropertyValues().add("transactionManagerBeanName",
                TxNamespaceHandler.getTransactionManagerName(element));
    }

    private void registerTransactionalEventListenerFactory(ParserContext parserContext) {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(TransactionalEventListenerFactory.class);
        parserContext.registerBeanComponent(new BeanComponentDefinition(def,
                TransactionManagementConfigUtils.TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME));
    }


    /**
     * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
     */
    private static class AopAutoProxyConfigurer {

        public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
            AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);      /**!!注册InternalAutoProxyCreator InfrastructureAdvisorAutoProxyCreator **/

            String txAdvisorBeanName = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
            if (!parserContext.getRegistry().containsBeanDefinition(txAdvisorBeanName)) {
                Object eleSource = parserContext.extractSource(element);

                // Create the TransactionAttributeSource definition.
                RootBeanDefinition sourceDef = new RootBeanDefinition(
                        "org.springframework.transaction.annotation.AnnotationTransactionAttributeSource");
                sourceDef.setSource(eleSource);
                sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);                      // 注册bean 用规则生成的bean名称

                // Create the TransactionInterceptor definition.
                RootBeanDefinition interceptorDef = new RootBeanDefinition(TransactionInterceptor.class);                       // advice 增强方法
                interceptorDef.setSource(eleSource);
                interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                registerTransactionManager(element, interceptorDef);
                interceptorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
                String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);            // 注册bean 用规则生成的bean名称

                // Create the TransactionAttributeSourceAdvisor definition.
                RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryTransactionAttributeSourceAdvisor.class);     // PointcutAdvisor 切面
                advisorDef.setSource(eleSource);
                advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                advisorDef.getPropertyValues().add("transactionAttributeSource", new RuntimeBeanReference(sourceName));
                advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);                              // 织入的增强是TransactionInterceptor
                if (element.hasAttribute("order")) {
                    advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
                }
                parserContext.getRegistry().registerBeanDefinition(txAdvisorBeanName, advisorDef);                              // 注册bean 用代码内置的名称

                CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);  // 注册复合bean component
                compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));                    // 将上面注册的3个bean组装起来成为component
                compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, txAdvisorBeanName));
                parserContext.registerComponent(compositeDef);                      // 注册复合bean component
            }
        }
    }

}