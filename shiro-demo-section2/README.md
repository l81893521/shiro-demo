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
