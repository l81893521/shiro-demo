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

ini配置部分和之前的相比将多出对url部分的配置[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/resources/shiro.ini)

其中最重要的就是[urls]部分的配置,其格式是： “url=拦截器[参数]，拦截器[参数]”；
即如果当前请求的url匹配[urls]部分的某个url模式，将会执行其配置的拦截器
* 比如anon拦截器表示匿名访问（即不需要登录即可访问）；
* authc拦截器表示需要身份认证通过后才能访问
* roles[admin]拦截器表示需要有admin角色授权才能访问
* 而perms["user:create"]拦截器表示需要有“user:create”权限才能访问

url模式使用Ant风格模式

Ant路径通配符支持?、*、**，注意通配符匹配不包括目录分隔符“/”：
* ?：匹配一个字符，如”/admin?”将匹配/admin1，但不匹配/admin
* `*`：匹配零个或多个字符串，如/admin*将匹配/admin、/admin123，但不匹配/admin/1
* **：匹配路径中的零个或多个路径，如/admin/**将匹配/admin/a或/admin/a/b。

url模式匹配顺序

url模式匹配顺序是按照在配置中的声明顺序匹配，即从头开始使用第一个匹配的url模式对应的拦截器链。如：

```
/bb/**=filter1
/bb/aa=filter2
/**=filter3
```

如果请求的url是“/bb/aa”，因为按照声明顺序进行匹配，那么将使用filter1进行拦截。

拦截器将在下一节详细介绍。接着我们来看看身份验证、授权及退出在web中如何实现。

### 7.4 身份验证(登录)

* 首先配置需要身份验证的url
```
#需要验证才能访问
/authenticated=authc
/role=authc,roles[admin]
/permission=authc,perms["user:create"]
```

即访问这些地址时会首先判断用户有没有登录，如果没有登录默会跳转到登录页面，默认是/login.jsp，
可以通过在[main]部分通过如下配置修改：
```
#默认是/login.jsp
authc.loginUrl=/login
```

* 登录servlet

```
@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String error = null;

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        token.setRememberMe(true);

        try {
            subject.login(token);
        } catch (UnknownAccountException e) {
            error = "用户名/密码错误";
        } catch (IncorrectCredentialsException e) {
            error = "用户名/密码错误";
        } catch (AuthenticationException e) {
            //其他错误，比如锁定，如果想单独处理请单独catch处理
            error = "其他错误：" + e.getMessage();
        }

        if(error != null) {//出错了，返回登录页面
            req.setAttribute("error", error);
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        } else {//登录成功
            req.getRequestDispatcher("/WEB-INF/jsp/loginSuccess.jsp").forward(req, resp);
        }
    }
```

* doGet请求时展示登录页面；
* doPost时进行登录，登录时收集username/password参数，然后提交给Subject进行登录。如果有错误再返回到登录页面；否则跳转到登录成功页面（此处应该返回到访问登录页面之前的那个页面，或者没有上一个页面时访问主页）。
* JSP页面请参考源码。[查看代码](https://github.com/l81893521/shiro-demo/tree/master/shiro-demo-section7/src/main/webapp/WEB-INF/jsp)


* 测试
首先输入http://localhost:8080/login进行登录,登录成功后接着可以访问http://localhost:8080/authenticated来显示当前登录的用户
(请根据自身环境决定地址是否输入项目名)

```
${subject.principal}身份验证已通过。
```

当前实现的一个缺点就是，永远返回到同一个成功页面（比如首页），在实际项目中比如支付时如果没有登录将跳转到登录页面，登录成功后再跳回到支付页面；
对于这种功能大家可以在登录时把当前请求保存下来，然后登录成功后再重定向到该请求即可。

Shiro内置了登录（身份验证）的实现：基于表单的和基于Basic的验证，其通过拦截器实现。

###7.5 基于Basic的拦截器身份验证

* shiro-basicfilterlogin.ini配置

```
[main]
authcBasic.applicationName=please login

perms.unauthorizedUrl=/unauthorized
roles.unauthorizedUrl=/unauthorized
[users]
zhang=123,admin
wang=123

[roles]
admin=user:*,menu:*

[urls]
/role=authcBasic,roles[admin]
```

* authcBasic是org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter类型的实例，其用于实现基于Basic的身份验证；applicationName用于弹出的登录框显示信息使用，如图：

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/image/1.png)

* [urls]部分配置了/role地址需要走authcBasic拦截器，即如果访问/role时还没有通过身份验证那么将弹出如上图的对话框进行登录，登录成功即可访问。

* 把shiroConfigLocations改为shiro-basicfilterlogin.ini即可。

* 输入http://localhost:8080/role，会弹出之前的Basic验证对话框输入“zhang/123”即可登录成功进行访问。(根据自身环境是否输入项目名)

### 7.6 基于表单的拦截器身份验证

基于表单的拦截器身份验证和【1】类似，但是更简单，因为其已经实现了大部分登录逻辑；
我们只需要指定：登录地址/登录失败后错误信息存哪/成功的地址即可。

* shiro-formfilterlogin.ini
```
[main]
authc.loginUrl=/formfilterlogin
authc.usernameParam=username
authc.passwordParam=password
authc.successUrl=/
authc.failureKeyAttribute=shiroLoginFailure

perms.unauthorizedUrl=/unauthorized
roles.unauthorizedUrl=/unauthorized

[users]
zhang=123,admin
wang=123

[roles]
admin=user:*,menu:*

[urls]
/static/**=anon
/formfilterlogin=authc
/role=authc,roles[admin]
/permission=authc,perms["user:create"]
```

* authc是org.apache.shiro.web.filter.authc.FormAuthenticationFilter类型的实例，其用于实现基于表单的身份验证；
* 通过loginUrl指定当身份验证时的登录表单；
* usernameParam指定登录表单提交的用户名参数名；
* passwordParam指定登录表单提交的密码参数名；
* successUrl指定登录成功后重定向的默认地址（默认是“/”）（如果有上一个地址会自动重定向带该地址）
* failureKeyAttribute指定登录失败时的request属性key（默认shiroLoginFailure）；这样可以在登录表单得到该错误key显示相应的错误消息；

* 把shiroConfigLocations改为shiro- formfilterlogin.ini即可。

* 登录servlet[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/java/web/servlet/FormFilterLoginServlet.java)
```
@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String errorClassName = (String)req.getAttribute("shiroLoginFailure");

        if(UnknownAccountException.class.getName().equals(errorClassName)) {
            req.setAttribute("error", "用户名/密码错误");
        } else if(IncorrectCredentialsException.class.getName().equals(errorClassName)) {
            req.setAttribute("error", "用户名/密码错误");
        } else if(errorClassName != null) {
            req.setAttribute("error", "未知错误：" + errorClassName);
        }

        req.getRequestDispatcher("/WEB-INF/jsp/formfilterlogin.jsp").forward(req, resp);
    }
```
在登录Servlet中通过shiroLoginFailure得到authc登录失败时的异常类型名，然后根据此异常名来决定显示什么错误消息。

* 测试
输入http://localhost:8080/role，会跳转到“/formfilterlogin”登录表单，提交表单如果authc拦截器登录成功后，会直接重定向会之前的地址“/role”；
假设我们直接访问“/formfilterlogin”的话登录成功将直接到默认的successUrl。(根据自身环境是否输入项目名)

### 7.7 授权

shiro.ini[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/resources/shiro.ini)

```
[main]
roles.unauthorizedUrl=/unauthorized
perms.unauthorizedUrl=/unauthorized
 [urls]
/role=authc,roles[admin]
/permission=authc,perms["user:create"]
```

* 通过unauthorizedUrl属性指定如果授权失败时重定向到的地址
* roles是org.apache.shiro.web.filter.authz.RolesAuthorizationFilter类型的实例，通过参数指定访问时需要的角色，如“[admin]”，如果有多个使用“，”分割，且验证时是hasAllRole验证，即且的关系
* Perms是org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter类型的实例，和roles类似，只是验证权限字符串。

RoleServlet[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/java/web/servlet/RoleServlet.java)
```
@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Subject subject = SecurityUtils.getSubject();
        subject.checkRole("admin");
        req.getRequestDispatcher("/WEB-INF/jsp/hasRole.jsp").forward(req, resp);
    }
```

PermissionServlet[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/java/web/servlet/PermissionServlet.java)
```
@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Subject subject = SecurityUtils.getSubject();
        subject.checkPermission("user:create");
        req.getRequestDispatcher("/WEB-INF/jsp/hasPermission.jsp").forward(req, resp);
}
```

* 首先访问http://localhost:8080/login, 使用帐号“zhang/123”进行登录
* 再访问http://localhost:8080/role或http://localhost:8080/permission时会成功（因为其授权成功了）
* 如果使用帐号“wang/123”登录成功后访问这两个地址会跳转到“/unauthorized”即没有授权页面
* (根据自身环境是否输入项目名)

### 7.8 退出

shiro.ini[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/resources/shiro.ini)
```
[urls]
/logout=anon
```

* LogoutServlet[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section7/src/main/java/web/servlet/LogoutServlet.java)

```
@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SecurityUtils.getSubject().logout();
        req.getRequestDispatcher("/WEB-INF/jsp/logoutSuccess.jsp").forward(req, resp);
    }
```

直接调用Subject.logout即可，退出成功后转发/重定向到相应页面即可。

* 测试

首先访问http://localhost:8080/login，使用帐号“zhang/123”进行登录，登录成功后访问/logout即可退出。

* Shiro也提供了logout拦截器用于退出，其是org.apache.shiro.web.filter.authc.LogoutFilter类型的实例，我们可以在shiro.ini配置文件中通过如下配置完成退出：
```
[main]
logout.redirectUrl=/login

[urls]
/logout2=logout
```

* 通过logout.redirectUrl指定退出后重定向的地址；
* 通过/logout2=logout指定退出url是/logout2。这样当我们登录成功后然后访问/logout2即可退出。