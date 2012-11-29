package org.stuartgunter.spring.beans.factory.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Registers the {@link org.springframework.beans.factory.xml.BeanDefinitionParser}s for the <code>sg</code> namespace
 */
public class SpringNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("builder-factory", new BuilderFactoryBeanDefinitionParser());
    }
}
