package org.stuartgunter.spring.beans.factory.xml;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FluentBeanWrapper;
import org.springframework.beans.FluentStyle;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * <p>This {@link org.springframework.beans.factory.FactoryBean} internally uses the specified builder class
 * to create an instance of the desired type.</p>
 *
 * <p>Expectations of this class are:
 * <ul>
 *     <li>Builder class must provide a default constructor</li>
 *     <li>Builder methods must follow either the property-naming style (e.g. using a common prefix) or use fully-specified method names</li>
 *     <li>All builder methods (e.g. {@code withXXX(..)} or {@code append(..)}) must have an arity of 1 and return the builder instance</li>
 *     <li>The build method (e.g. {@code build()}) must have an arity of 0</li>
 * </ul>
 * </p>
 *
 * <p>The build method and method prefix can be customised to each use case.</p>
 */
public class BuilderFactoryBean extends AbstractFactoryBean {

    private Class<?> builderClass;
    private String buildMethod;
    private String methodPrefix;
    private FluentStyle fluentStyle;
    private Map<String, List<Object>> builderProperties;

    /**
     * The type of builder
     */
    public void setBuilderClass(Class<?> builderClass) {
        this.builderClass = builderClass;
    }

    /**
     * The name of the build method. This is the method that constructs the bean.
     * Defaults to {@code build}.
     */
    public void setBuildMethod(String buildMethod) {
        this.buildMethod = buildMethod;
    }

    /**
     * The name of the builder method prefix. This is the prefix applied to all methods that set properties on the builder.
     * Defaults to {@code with}.
     */
    public void setMethodPrefix(String methodPrefix) {
        this.methodPrefix = methodPrefix;
    }

    /**
     * The fluent style of the bean. This determines how the appropriate methods on the target bean class are discovered.
     */
    public void setFluentStyle(FluentStyle fluentStyle) {
        this.fluentStyle = fluentStyle;
    }

    /**
     * The properties to apply to the builder before finally constructing the bean.
     */
    public void setBuilderProperties(Map<String, List<Object>> builderProperties) {
        this.builderProperties = builderProperties;
    }

    @Override
    public Class<?> getObjectType() {
        return findBuildMethodReturnType();
    }

    @Override
    protected Object createInstance() throws Exception {
        Object builder = BeanUtils.instantiate(builderClass);
        FluentBeanWrapper fluentBeanWrapper = new FluentBeanWrapper(builder, methodPrefix, buildMethod, fluentStyle);
        fluentBeanWrapper.setBeanFactory(getBeanFactory());

        for (Map.Entry<String, List<Object>> builderProperty : builderProperties.entrySet()) {
            String propertyname = builderProperty.getKey();
            for (Object propertyValue : builderProperty.getValue()) {
                fluentBeanWrapper.setPropertyValue(propertyname, propertyValue);
            }
        }

        return fluentBeanWrapper.build();
    }

    private Class<?> findBuildMethodReturnType() {
        Method method = BeanUtils.findDeclaredMethod(builderClass, buildMethod, null);
        if (method == null) {
            throw new RuntimeException("Build method '" + buildMethod + "' could not be found");
        } else {
            return method.getReturnType();
        }
    }
}
