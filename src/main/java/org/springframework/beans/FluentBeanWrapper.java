package org.springframework.beans;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Add JavaDoc
 */
public class FluentBeanWrapper extends AbstractPropertyAccessor implements BeanFactoryAware {

    private final Object target;
    private final Map<String, Method> fluentProperties = new HashMap<String, Method>();
    private final TypeConverterDelegate typeConverterDelegate;

    private BeanFactory beanFactory;

    public FluentBeanWrapper(final Object target, final String fluentMethodPrefix) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
        this.typeConverterDelegate = new TypeConverterDelegate(this, target);
        registerDefaultEditors();
        setExtractOldValueForEditor(true);

        ReflectionUtils.doWithMethods(target.getClass(), new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getName().startsWith(fluentMethodPrefix)
                        && method.getParameterTypes().length == 1
                        && target.getClass().isAssignableFrom(method.getReturnType())) {
                    String propertyName = method.getName().substring(fluentMethodPrefix.length());
                    String normalisedPropertyName = propertyName.subSequence(0, 1).toString().toLowerCase() + propertyName.substring(1);
                    fluentProperties.put(normalisedPropertyName, method);
                }
            }
        });
    }

    @Override
    public boolean isReadableProperty(String propertyName) throws BeansException {
        return false;
    }

    @Override
    public boolean isWritableProperty(String propertyName) throws BeansException {
        return this.fluentProperties.containsKey(propertyName);
    }

    @Override
    public Class<?> getPropertyType(String propertyName) throws BeansException {
        Method method = this.fluentProperties.get(propertyName);
        if (method != null) {
            return method.getParameterTypes()[0];
        }
        return null;
    }

    @Override
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
        Method method = this.fluentProperties.get(propertyName);
        if (method != null) {
            return new TypeDescriptor(new MethodParameter(method, 0));
        }
        return null;
    }

    @Override
    public Object getPropertyValue(String propertyName) throws BeansException {
        throw new InvalidPropertyException(this.target.getClass(), propertyName, "Property is not accessible");
    }

    @Override
    public void setPropertyValue(String propertyName, Object newValue) throws BeansException {
        Method method = this.fluentProperties.get(propertyName);
        if (method == null) {
            throw new NotWritablePropertyException(
                    this.target.getClass(), propertyName, "Property '" + propertyName + "' does not exist");
        }
        Class<?> paramType = method.getParameterTypes()[0];
        if (newValue instanceof RuntimeBeanReference) {
            String beanName = ((RuntimeBeanReference) newValue).getBeanName();
            newValue = beanFactory.getBean(beanName);
        }
        try {
            Object convertedValue = this.typeConverterDelegate.convertIfNecessary(propertyName, null, newValue,
                    paramType, new TypeDescriptor(new MethodParameter(method, 0)));
            method.invoke(this.target, convertedValue);
        }
        catch (ConverterNotFoundException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, null, newValue);
            throw new ConversionNotSupportedException(pce, paramType, ex);
        }
        catch (ConversionException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, null, newValue);
            throw new TypeMismatchException(pce, paramType, ex);
        }
        catch (IllegalArgumentException ex) {
            PropertyChangeEvent pce = new PropertyChangeEvent(this.target, propertyName, null, newValue);
            throw new TypeMismatchException(pce, paramType, ex);
        }
        catch (IllegalAccessException ex) {
            throw new InvalidPropertyException(this.target.getClass(), propertyName, "Property is not accessible", ex);
        }
        catch (InvocationTargetException ex) {
            throw new InvalidPropertyException(this.target.getClass(), propertyName, "Fluent method threw an exception", ex);
        }
    }

    @Override
    public <T> T convertIfNecessary(
            Object value, Class<T> requiredType, MethodParameter methodParam) throws TypeMismatchException {
        try {
            return this.typeConverterDelegate.convertIfNecessary(value, requiredType, methodParam);
        }
        catch (IllegalArgumentException ex) {
            throw new TypeMismatchException(value, requiredType, ex);
        }
        catch (IllegalStateException ex) {
            throw new ConversionNotSupportedException(value, requiredType, ex);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
