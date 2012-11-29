package org.stuartgunter.spring.beans.factory.xml;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses any {@code sg:builder-factory} XML elements in Spring configuration.
 */
public class BuilderFactoryBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return BuilderFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        configureAttributeForBuilder("target-class", "targetClass", element, builder);
        configureAttributeForBuilder("builder-class", "builderClass", element, builder);
        configureAttributeForBuilder("method-prefix", "methodPrefix", "with", element, builder);
        configureAttributeForBuilder("build-method", "buildMethod", "build", element, builder);

        Map<String, Object> builderProperties = new HashMap<String, Object>();
        List<Element> properties = DomUtils.getChildElementsByTagName(element, "with");
        for (Element property : properties) {
            String name = property.getAttribute("name");
            builderProperties.put(name, createBuilderProperty(property));
        }
        builder.addPropertyValue("builderProperties", builderProperties);
    }

    private Object createBuilderProperty(Element property) {
        if (property.hasAttribute("value")) {
            return property.getAttribute("value");
        } else {
            return new RuntimeBeanReference(property.getAttribute("ref"));
        }
    }

    private void configureAttributeForBuilder(String attributeName, String propertyName,
                                              Element element, BeanDefinitionBuilder builder) {
        String attributeValue = element.getAttribute(attributeName);
        if (StringUtils.hasText(attributeValue)) {
            builder.addPropertyValue(propertyName, attributeValue);
        }
    }

    private void configureAttributeForBuilder(String attributeName, String propertyName, String defaultValue,
                                              Element element, BeanDefinitionBuilder builder) {
        String attributeValue = element.getAttribute(attributeName);
        Object value = StringUtils.hasText(attributeValue) ? attributeValue : defaultValue;
        builder.addPropertyValue(propertyName, value);
    }
}
