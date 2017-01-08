package com.linkedkeeper.configcenter.client.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理类一定要实现InvocationHandler接口，就要实现invoke()方法
 * <p/>
 * Created by frank@linkedkeeper.com on 17/1/8.
 */
public class DefinedConfigCenterClientHandler implements InvocationHandler {

    private Object targetObject;

    /*
     * 下面方法是得到代理对象，如果得不到代理对象，这个效果也是没有作用的
     * 最后一个参数是InvocationHandler接口，这也是为什么动态代理对象一定要实现这个接口的原因
     * 得到的代理对象会执行invoke()方法
     */
    public Object newProxy(Object targetObject) {
        this.targetObject = targetObject;
        //得到代理对象的方法，这个是反射机制里面的对象方法
        return Proxy.newProxyInstance(targetObject.getClass().getClassLoader(),
                targetObject.getClass().getInterfaces(),
                this);
    }

    /**
     * 在代理对象之前做业务处理，然后再做对象定义的方法，因此最后要返回代理的对象
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(this.targetObject, args);
    }

}
