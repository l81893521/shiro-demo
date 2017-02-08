###7.1 准备环境

1. 创建webapp应用

2. 依赖

Servlet3
```
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.0.1</version>
    <scope>provided</scope>
</dependency>
```
shiro-web
```
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-web</artifactId>
    <version>1.2.1</version>
</dependency>
```
其他依赖请参考源码的pom.xml

###7.2 ShiroFilter入口

* Shiro 1.2及以后版本的配置方式

从Shiro 1.2开始引入了Environment/WebEnvironment的概念，即由它们的实现提供相应的SecurityManager及其相应的依赖。
ShiroFilter会自动找到Environment然后获取相应的依赖。
```
<listener>
    <listener-class>org.apache.shiro.web.env.EnvironmentLoaderListener</listener-class>
</listener>
```
通过EnvironmentLoaderListener来创建相应的WebEnvironment，并自动绑定到ServletContext，默认使用IniWebEnvironment实现。

可以通过如下配置修改默认实现及其加载的配置文件位置：
```
<!--默认就是IniWebEnvironment,这里只是说明一下可以修改默认-->
<context-param>
    <param-name>shiroEnvironmentClass</param-name>
    <param-value>org.apache.shiro.web.env.IniWebEnvironment</param-value>
</context-param>
<!-- 默认先从/WEB-INF/shiro.ini，如果没有找classpath:shiro.ini -->
<context-param>
    <param-name>shiroConfigLocations</param-name>
    <param-value>classpath:shiro.ini</param-value>
</context-param>
```
shiroConfigLocations默认是“/WEB-INF/shiro.ini”，IniWebEnvironment默认是先从/WEB-INF/shiro.ini加载，如果没有就默认加载classpath:shiro.ini。

* 与Spring集成

```
<filter>
    <filter-name>shiroFilter</filter-name>
    <filter-class>org.apache.shiro.web.servlet.ShiroFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>shiroFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```
DelegatingFilterProxy作用是自动到spring容器查找名字为shiroFilter（filter-name）的bean并把所有Filter的操作委托给它。然后将ShiroFilter配置到spring容器即可

```
<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
<property name="securityManager" ref="securityManager"/>
    <!—忽略其他，详见与Spring集成部分 -->
</bean>
```

最后不要忘了使用org.springframework.web.context.ContextLoaderListener加载这个spring配置文件即可。
因为我们现在的shiro版本是1.2的，因此之后的测试都是使用1.2的配置。

###7.3 Web INI配置
```
[main]
#默认是/login.jsp
authc.loginUrl=/login
roles.unauthorizedUrl=/unauthorized
perms.unauthorizedUrl=/unauthorized

logout.redirectUrl=/login

[users]
zhang=123,admin
wang=123

[roles]
admin=user:*,menu:*

[urls]
/logout2=logout
/login=anon
/logout=anon
/unauthorized=anon
/static/**=anon
/authenticated=authc
/role=authc,roles[admin]
/permission=authc,perms["user:create"]
```

ini配置部分和之前的相比将多出对url部分的配置[查看代码]()
