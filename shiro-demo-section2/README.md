### 2.1 介绍

**身份验证** : 即在应用中谁能证明他就是他本人。一般提供如他们的身份ID一些标识信息来表明他就是他本人，如提供身份证，用户名/密码来证明。

在shiro中，用户需要提供principals （身份）和credentials（证明）给shiro，从而应用能验证用户身份：

**principals** : 身份，即主体的标识属性，可以是任何东西，如用户名、邮箱等，唯一即可。
一个主体可以有多个principals，但只有一个Primary principals，一般是用户名/密码/手机号。

**credentials** : 证明/凭证，即只有主体知道的安全值，如密码/数字证书等。

最常见的principals和credentials组合就是用户名/密码了。接下来先进行一个基本的身份认证。

另外两个相关的概念是之前提到的**Subject**和**Realm**,分别是主体及验证主体的数据源。

### 2.2 环境准备
本文使用Maven构建，因此需要一点Maven知识。首先准备环境依赖：

```
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-core</artifactId>
    <version>1.2.2</version>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
</dependency>
<dependency>
    <groupId>commons-logging</groupId>
    <artifactId>commons-logging</artifactId>
    <version>1.1.3</version>
</dependency>
```
添加junit、common-logging及shiro-core依赖即可。

### 2.3 登录/登出
1. 准备用户身份/凭据（shiro.ini）[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/src/test/resources/shiro.ini)
```
[users]
zhang=123
zhangsan=123
```
2. 测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/src/test/java/org/shiro/demo/section1/LoginLogoutTest.java)
```
@Test
public void testHelloworld(){
    //1.获取SecurityManagerFactory,此处用shiro.ini来初始化
    Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
    //2.得到securityManager实例
    SecurityManager securityManager = factory.getInstance();
    //3.绑定给securityManager
    SecurityUtils.setSecurityManager(securityManager);
    //4.获取subject
    Subject subject = SecurityUtils.getSubject();

    UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");

    try {
        //5.登录
        subject.login(token);
    } catch (Exception e) {
        //6.身份验证失败
    }

    Assert.assertEquals(true, subject.isAuthenticated());

    //7.登出
    subject.logout();
}
```
2.1 首先通过new IniSecurityManagerFactory并指定一个ini配置文件来创建一个SecurityManager工厂

2.2 接着获取SecurityManager并绑定到SecurityUtils，这是一个全局设置，设置一次即可

2.3 通过SecurityUtils得到Subject，其会自动绑定到当前线程；

如果在web环境在请求结束时需要解除绑定；然后获取身份验证的Token，如用户名/密码

2.4 调用subject.login方法进行登录，其会自动委托给SecurityManager.login方法进行登录

2.5 如果身份验证失败请捕获AuthenticationException或其子类，常见的如：
    DisabledAccountException（禁用的帐号）、LockedAccountException（锁定的帐号）、UnknownAccountException（错误的帐号）、
    ExcessiveAttemptsException（登录失败次数过多）、IncorrectCredentialsException （错误的凭证）、
    ExpiredCredentialsException（过期的凭证）等，具体请查看其继承关系

2.6 最后可以调用subject.logout退出，其会自动委托给SecurityManager.logout方法退出

**从如上代码可总结出身份验证的步骤**

1. 收集用户身份/凭证，即如用户名/密码

2. 调用Subject.login进行登录，如果失败将得到相应的AuthenticationException异常，根据异常提示用户错误信息；否则登录成功

3. 最后调用Subject.logout进行退出操作

**如上测试的几个问题**

1. 用户名/密码硬编码在ini配置文件，以后需要改成如数据库存储，且密码需要加密存储

2. 用户身份Token可能不仅仅是用户名/密码，也可能还有其他的，如登录时允许用户名/邮箱/手机号同时登录

### 2.4 身份认证流程
![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/images/1.png)

1. 首先调用Subject.login(token)进行登录，其会自动委托给Security Manager，调用之前必须通过SecurityUtils. setSecurityManager()设置

2. SecurityManager负责真正的身份验证逻辑；它会委托给Authenticator进行身份验证

3. Authenticator才是真正的身份验证者，Shiro API中核心的身份认证入口点，此处可以自定义插入自己的实现

4. Authenticator可能会委托给相应的AuthenticationStrategy进行多Realm身份验证，
默认ModularRealmAuthenticator会调用AuthenticationStrategy进行多Realm身份验证

5. Authenticator会把相应的token传入Realm，从Realm获取身份验证信息，如果没有返回/抛出异常表示身份验证失败了。
此处可以配置多个Realm，将按照相应的顺序及策略进行访问

### 2.5 Realm
**Realm:** 域，Shiro从从Realm获取安全数据（如用户、角色、权限），就是说SecurityManager要验证用户身份，
那么它需要从Realm获取相应的用户进行比较以确定用户身份是否合法；也需要从Realm得到用户相应的角色/权限进行验证用户是否能进行操作；
可以把Realm看成DataSource，即安全数据源。如我们之前的ini配置方式将使用org.apache.shiro.realm.text.IniRealm。

**org.apache.shiro.realm.Realm 接口如下:**
```
public interface Realm {
    /**
     * 返回一个唯一的Realm名字
     */
    String getName();
    /**
     * 判断此Realm是否支持此token
     */
    boolean supports(AuthenticationToken var1);
    /**
     * 根据Token获取认证信息
     */
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken var1) throws AuthenticationException;
}
```

**单Realm配置:**

1. 自定义Realm实现 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/src/test/java/org/shiro/demo/section1/realm/MyRealm1.java)

```
public class MyRealm1 implements Realm{
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = (String) token.getPrincipal();
        String password = new String((char[])token.getCredentials());
        if(!"zhang".equals(username)){
            //用户名错误
            throw new UnknownAccountException();
        }
        if(!"123".equals(password)){
            //密码错误
            throw new IncorrectCredentialsException();
        }
        //认证成功 返回一个Authentication的实现
        return new SimpleAuthenticationInfo(username, password, getName());
    }

    public String getName() {
        return "myRealm1";
    }

    public boolean supports(AuthenticationToken token) {
        // 仅支持usernamePasswordToken
        return token instanceof UsernamePasswordToken;
    }
}
```

2. ini配置文件指定自定义Realm实现(shiro-realm.ini) [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/src/test/resources/shiro-realm.ini)
```
#声明一个realm
myRealm1=org.shiro.demo.section1.realm.MyRealm1
#指定securityManager的realm实现
securityManager.realms=$myRealm1
```

通过$name来引入之前的realm定义

3. 测试用例testCustomRealm方法 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section2/src/test/java/org/shiro/demo/section1/LoginLogoutTest.java)

只需要把之前的shiro.ini配置文件改成shiro-realm.ini即可。

**多Realm配置**