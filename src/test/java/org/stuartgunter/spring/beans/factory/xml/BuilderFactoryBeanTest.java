package org.stuartgunter.spring.beans.factory.xml;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@Test
public class BuilderFactoryBeanTest {

    GenericXmlApplicationContext applicationContext;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        applicationContext = new GenericXmlApplicationContext();
    }

    @AfterMethod
    public void afterMethod() {
        applicationContext.close();
    }

    private void loadBeanDefinitions(String fileName) {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        ClassPathResource resource = new ClassPathResource(fileName);
        reader.loadBeanDefinitions(resource);
        applicationContext.refresh();
    }

    public void shouldCreateBeanUsingDefaults() {
        loadBeanDefinitions("default-bean-factory.xml");

        final Object bean = applicationContext.getBean("testBean");

        assertNotNull(bean);
        assertEquals("HELLO WORLD!", bean);
    }

    public void shouldCreateBeanUsingCustomProperties() {
        loadBeanDefinitions("customised-bean-factory.xml");

        final Object bean = applicationContext.getBean("testBean");

        assertNotNull(bean);
        assertEquals("Hello World!", bean);
    }

    public void shouldCreateBeanUsingFluentMethods() {
        loadBeanDefinitions("fluent-method-bean-factory.xml");

        final Object bean = applicationContext.getBean("testBean");

        assertNotNull(bean);
        assertEquals("Hello World!", bean);
    }
}
