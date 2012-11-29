package org.stuartgunter.spring.beans.factory.xml;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * TODO: Add JavaDoc
 */
public class FluentBeanWrapper extends BeanWrapperImpl {

    private final String writeMethodPrefix;

    public FluentBeanWrapper(Object object, String writeMethodPrefix) {
        super(object);

        Assert.notNull(writeMethodPrefix, "Write method prefix must not be null");
        this.writeMethodPrefix = writeMethodPrefix;
    }

    @Override
    protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
        Assert.notNull(propertyName, "Property name must not be null");

        String writeMethodName = writeMethodPrefix + StringUtils.capitalize(propertyName);
        try {
            return new PropertyDescriptor(propertyName, getWrappedClass(), "", writeMethodName);
        } catch (IntrospectionException ex) {
            throw new BeanInstantiationException(getWrappedClass(),
                    "Could not determine fluent write method for bean factory", ex);
        }
    }
}
