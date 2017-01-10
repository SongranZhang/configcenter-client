# configcenter-client
A Java library for Configuration Management Client implemented by Zookeeper.

#Installation
Clone this repository, and add it as a dependent maven project

#Usage
##Create a client
###Config Spring Xml
```xml
<bean id="client" class="com.linkedkeeper.configcenter.client.impl.FailoverConfigCenterClient"
          init-method="init" destroy-method="destroy">
    <property name="zkServers" value="127.0.0.1:2181"/>
    <property name="zNodePath" value="/configcenter/test"/>
    <property name="storeFilePath" value="/configcenter/snapshot/"/>
    <property name="recoveryFilePath" value="/configcenter/recovery/"/>
    <!-- 输入则回调自定义监听 -->
    <!--<property name="dataListener" ref="definedClientListener"/>-->
</bean>
```
###Recovery Config Xml
```xml
<bean id="configcenterStoreHandler" 
          class="com.linkedkeeper.configcenter.recovery.RecoveryStoreHandler">
    <property name="client" ref="client"/>
    <property name="recoveryFilePath" value="/configcenter/recovery/"/>
</bean>
<!-- recovery timer -->
<bean id="recoveryQuartz" 
          class="org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean">
    <property name="targetObject" ref="configcenterStoreHandler"/>
    <property name="targetMethod" value="recovery"/>
    <property name="concurrent" value="false"/>
</bean>
<bean id="recoveryTrigger" 
          class="org.springframework.scheduling.quartz.CronTriggerFactoryBean">
    <property name="jobDetail" ref="recoveryQuartz"/>
    <property name="cronExpression" value="0 0/10 * * * ?"/>
</bean>
<bean id="recoveryScheduler" 
          class="org.springframework.scheduling.quartz.SchedulerFactoryBean">
    <property name="triggers">
        <list>
            <ref bean="recoveryTrigger"/>
        </list>
    </property>
    <property name="quartzProperties">
        <props>
            <prop key="org.quartz.threadPool.threadCount">1</prop>
        </props>
    </property>
    <property name="autoStartup">
        <value>true</value>
    </property>
</bean>
```
###Junit Test
```java
public class TestClient extends BaseTest {

    private ConfigCenterClient client;

    @Test
    public void test() {
        String key = "com.linkedkeeper.test";
        String value = UUID.randomUUID().toString();
        
        client.set(key, value);
        
        String zkValue = client.get(key);
        System.out.println(zkValue);
    }

    public void setClient(ConfigCenterClient client) {
        this.client = client;
    }
}
```
##License

Licensed under the New 3-Clause BSD License.
```
Copyright (c) 2016, LinkedKeeper
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of LinkedKeeper nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
