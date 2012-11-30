package org.springframework.beans;

import com.google.common.collect.*;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * {@link PropertyAccessor} implementation that accesses 'fluent' methods on the target bean.
 * Allows for invocation of either via consistently named builder-style methods or uniquely named independent methods.
 *
 * <p>This implementation just supports methods in the actual target object.
 * It is not able to traverse nested fields.
 */
public class FluentBeanWrapper extends AbstractPropertyAccessor implements BeanFactoryAware {

    private final Object target;
    private final FluentStyle fluentStyle;
    private final String fluentMethodPrefix;

    // this is a multimap to handle overloaded methods
    private final ListMultimap<String, Method> properties = ArrayListMultimap.create();

    private final TypeConverterDelegate typeConverterDelegate;

    private BeanFactory beanFactory;

    public FluentBeanWrapper(final Object target, final String fluentMethodPrefix, final FluentStyle fluentStyle) {
        Assert.notNull(target, "Target object must not be null");
        this.target = target;
        this.fluentStyle = fluentStyle;
        this.fluentMethodPrefix = fluentMethodPrefix;
        this.typeConverterDelegate = new TypeConverterDelegate(this, target);
        registerDefaultEditors();

        if (fluentStyle == FluentStyle.PROPERTIES) {
            registerFluentProperties(fluentMethodPrefix);
        } else {
            registerFluentMethods();
        }
    }

    private void registerFluentMethods() {
        ReflectionUtils.doWithMethods(target.getClass(),
                new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        properties.put(method.getName(), method);
                    }
                },
                new ReflectionUtils.MethodFilter() {
                    @Override
                    public boolean matches(Method method) {
                        return method.getParameterTypes().length == 1
                                && target.getClass().isAssignableFrom(method.getReturnType());
                    }
                });
    }

    private void registerFluentProperties(final String fluentMethodPrefix) {
        ReflectionUtils.doWithMethods(target.getClass(),
                new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        String propertyName = method.getName().substring(fluentMethodPrefix.length());
                        properties.put(StringUtils.uncapitalize(propertyName), method);
                    }
                },
                new ReflectionUtils.MethodFilter() {
                    @Override
                    public boolean matches(Method method) {
                        return method.getName().startsWith(fluentMethodPrefix)
                                && method.getParameterTypes().length == 1
                                && target.getClass().isAssignableFrom(method.getReturnType());
                    }
                });
    }

    @Override
    public boolean isReadableProperty(String propertyName) throws BeansException {
        return false;
    }

    @Override
    public boolean isWritableProperty(String propertyName) throws BeansException {
        return this.properties.containsKey(propertyName);
    }

    @Override
    public Class<?> getPropertyType(String propertyName) throws BeansException {
        // return the hinted type if available otherwise the most general type of all the overloads?

//        Method method = this.properties.get(propertyName);
//        if (method != null) {
//            return method.getParameterTypes()[0];
//        }
        return null;
    }

    @Override
    public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
//        Method method = this.properties.get(propertyName);
//        if (method != null) {
//            return new TypeDescriptor(new MethodParameter(method, 0));
//        }
        return null;
    }

    @Override
    public Object getPropertyValue(String propertyName) throws BeansException {
        throw new InvalidPropertyException(this.target.getClass(), propertyName, "Property is not accessible");
    }

    @Override
    public void setPropertyValue(String propertyName, Object newValue) throws BeansException {
        setPropertyValue(propertyName, newValue, null);
    }

    public void setPropertyValue(String propertyName, Object newValue, Class<?> type) throws BeansException {
        Method method = getFluentMethod(propertyName, type);
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

    private Method getFluentMethod(String propertyName, Class<?> type) {
        List<Method> methods = properties.get(propertyName);
        if (methods.isEmpty()) {
            return null;
        } else if (methods.size() == 1) {
            return methods.get(0);
        }

        String methodName = (fluentStyle == FluentStyle.METHODS)
                ? propertyName
                : fluentMethodPrefix + StringUtils.uncapitalize(propertyName);
        return MethodUtils.getMatchingAccessibleMethod(target.getClass(), methodName, new Class[] {type});
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
