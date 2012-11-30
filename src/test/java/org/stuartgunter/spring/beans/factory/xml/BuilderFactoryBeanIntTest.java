package org.stuartgunter.spring.beans.factory.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

@Test
@ContextConfiguration("classpath:/test-app-context.xml")
public class BuilderFactoryBeanIntTest extends AbstractTestNGSpringContextTests {

    public void shouldGetMyBean() {
        final Object bean = applicationContext.getBean("myBean");

        assertNotNull(bean);
        assertEquals("HELLO WORLD!", bean);
    }

    public void shouldGetMySpecialBean() {
        final Object bean = applicationContext.getBean("mySpecialBean");

        assertNotNull(bean);
        assertEquals("Hello World!", bean);
    }

    public void shouldGetStringBuilderBean() {
        final Object bean = applicationContext.getBean("stringBuilderBean");

        assertNotNull(bean);
        assertEquals("Hello World!", bean);
    }

    public void shouldGetImmutableListBean() {
        final List<String> bean = (List<String>) applicationContext.getBean("immutableListBean");

        assertNotNull(bean);
        assertTrue(bean instanceof ImmutableList);
        assertArrayEquals(new String[] {"Hello", " World", "!"}, bean.toArray());
    }
}
