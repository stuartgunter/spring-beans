package org.stuartgunter.spring.beans.factory.xml;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.FluentStyle;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

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
        configureAttributeForBuilder("fluent-style", "fluentStyle", FluentStyle.PROPERTIES, element, builder);
        configureAttributeForBuilder("builder-class", "builderClass", element, builder);
        configureAttributeForBuilder("method-prefix", "methodPrefix", "with", element, builder);
        configureAttributeForBuilder("build-method", "buildMethod", "build", element, builder);

        Map<String, List<Object>> builderProperties = Maps.newHashMap();
        List<Element> properties = DomUtils.getChildElementsByTagName(element, "with");
        for (Element property : properties) {
            String name = property.getAttribute("name");
            List<Object> values = builderProperties.get(name);
            if (values == null) {
                values = Lists.newArrayList();
                builderProperties.put(name, values);
            }
            values.add(createBuilderProperty(property, parserContext));
        }
        builder.addPropertyValue("builderProperties", builderProperties);
    }

    private Object createBuilderProperty(Element property, ParserContext parserContext) {
        if (property.hasAttribute("value") && property.hasAttribute("ref")) {
            parserContext.getReaderContext()
                    .fatal("A 'with' element may either have a 'value' or 'ref' attribute, but not both", property);
        }

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

    private void configureAttributeForBuilder(String attributeName, String propertyName, Object defaultValue,
                                              Element element, BeanDefinitionBuilder builder) {
        String attributeValue = element.getAttribute(attributeName);
        Object value = StringUtils.hasText(attributeValue) ? attributeValue : defaultValue;
        builder.addPropertyValue(propertyName, value);
    }
}
