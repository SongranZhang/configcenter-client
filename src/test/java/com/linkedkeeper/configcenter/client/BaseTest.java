package com.linkedkeeper.configcenter.client;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class BaseTest {

    private static ApplicationContext context;

    @BeforeClass
    public static void setUp() {
        Long startTime = System.currentTimeMillis();
        System.out.println("loading config file...");
        context = new ClassPathXmlApplicationContext(new String[]{"spring-configcenter-client.xml"});
        System.out.println("loading config file finish take time" + (System.currentTimeMillis() - startTime));
    }

    @Before
    public void setBean() throws Exception {
        if (context != null)
            context.getAutowireCapableBeanFactory().autowireBeanProperties(this, DefaultListableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
}
