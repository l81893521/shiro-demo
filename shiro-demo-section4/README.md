之前章节我们已经接触过一些INI配置规则了，如果大家使用过如Spring之类的IoC/DI容器的话，Shiro提供的INI配置也是非常类似的，
即可以理解为是一个IoC/DI容器，但是区别在于它从一个根对象securityManager开始。

###4.1 根对象SecurityManager

从之前的Shiro架构图可以看出，Shiro是从根对象SecurityManager进行身份验证和授权的；
也就是所有操作都是自它开始的，这个对象是线程安全且真个应用只需要一个即可，因此Shiro提供了SecurityUtils让我们绑定它为全局的，方便后续操作。

因为Shiro的类都是POJO的，因此都很容易放到任何IoC容器管理。
但是和一般的IoC容器的区别在于，Shiro从根对象securityManager开始导航；
Shiro支持的依赖注入：public空参构造器对象的创建、setter依赖注入。

纯java代码写法 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section4/src/test/java/NonConfigurationCreateTest.java)
```
@Test
public void test(){

    DefaultSecurityManager securityManager = new DefaultSecurityManager();

    //设置authenticator
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
    securityManager.setAuthenticator(authenticator);

    //设置authorizer
    ModularRealmAuthorizer authorizer = new ModularRealmAuthorizer();
    authorizer.setPermissionResolver(new WildcardPermissionResolver());
    securityManager.setAuthorizer(authorizer);

    //创建datasource
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.mysql.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://192.168.31.188:3306/shiro");
    dataSource.setUsername("root");
    dataSource.setPassword("YEEkoo@2016");

    //设置real
    JdbcRealm realm = new JdbcRealm();
    realm.setDataSource(dataSource);
    realm.setPermissionsLookupEnabled(true);
    securityManager.setRealm(realm);

    //将SecurityManager设置到SecurityUtils,方便全局使用
    SecurityUtils.setSecurityManager(securityManager);


    Subject subject = SecurityUtils.getSubject();
    UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
    subject.login(token);

    Assert.assertTrue(subject.isAuthenticated());
}
```
等价的ini配置 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section4/src/test/resources/shiro-config.ini)
```
#默认就是DefaultSecurityManager,可写可不写,也可以覆盖
#securityManager=org.apache.shiro.mgt.DefaultSecurityManager

#authenticator
authenticator=org.apache.shiro.authc.pam.ModularRealmAuthenticator
authenticationStrategy=org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy
authenticator.authenticationStrategy=$authenticationStrategy
securityManager.authenticator=$authenticator

#authorizer
authorizer=org.apache.shiro.authz.ModularRealmAuthorizer
permissionResolver=org.apache.shiro.authz.permission.WildcardPermissionResolver
authorizer.permissionResolver=$permissionResolver
securityManager.authorizer=$authorizer

#datasource
dataSource=com.alibaba.druid.pool.DruidDataSource
dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql://192.168.31.188:3306/shiro
dataSource.username=root
dataSource.password=YEEkoo@2016

#realm
jdbcRealm=org.apache.shiro.realm.jdbc.JdbcRealm
jdbcRealm.dataSource=$dataSource
jdbcRealm.permissionsLookupEnabled=true
securityManager.realms=$jdbcRealm
```
测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section4/src/test/java/ConfigurationCreateTest.java)
```
@Test
public void test(){
    Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-config.ini");
    SecurityManager securityManager = factory.getInstance();
    SecurityUtils.setSecurityManager(securityManager);

    Subject subject = SecurityUtils.getSubject();
    UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
    subject.login(token);

    Assert.assertTrue(subject.isAuthenticated());
}
```

1. 默认情况先创建一个名字为securityManager，类型为org.apache.shiro.mgt.DefaultSecurityManager的默认的SecurityManager，
如果想自定义，只需要在ini配置文件中指定“securityManager=SecurityManager实现类”即可，名字必须为securityManager，
它是起始的根；
2. IniSecurityManagerFactory是创建securityManager的工厂，其需要一个ini配置文件路径，
其支持“classpath:”（类路径）、“file:”（文件系统）、“url:”（网络）三种路径格式，默认是文件系统；
3. 接着获取SecuriyManager实例，后续步骤和之前的一样。

###4.2 INI配置

ini配置文件类似于Java中的properties（key=value），不过提供了将key/value分类的特性，
key是每个部分不重复即可，而不是整个配置文件。

如下是INI配置分类：
```
[main]
#提供了对根对象securityManager及其依赖的配置
securityManager=org.apache.shiro.mgt.DefaultSecurityManager
…………
securityManager.realms=$jdbcRealm

[users]
#提供了对用户/密码及其角色的配置，用户名=密码，角色1，角色2
username=password,role1,role2

[roles]
#提供了角色及权限之间关系的配置，角色=权限1，权限2
role1=permission1,permission2

[urls]
#用于web，提供了对web url拦截相关的配置，url=拦截器[参数]，拦截器
/index.html = anon
/admin/** = authc, roles[admin], perms["permission1"]
```

**[main]部分**

提供了对根对象securityManager及其依赖对象的配置。
```
securityManager=org.apache.shiro.mgt.DefaultSecurityManager
```
其构造器必须是public空参构造器，通过反射创建相应的实例。

**常量值setter注入**
```
dataSource.driverClassName=com.mysql.jdbc.Driver
jdbcRealm.permissionsLookupEnabled=true
```
会自动调用jdbcRealm.setPermissionsLookupEnabled(true)，对于这种常量值会自动类型转换。

**对象引用setter注入**
```
authenticator=org.apache.shiro.authc.pam.ModularRealmAuthenticator
authenticationStrategy=org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy
authenticator.authenticationStrategy=$authenticationStrategy
securityManager.authenticator=$authenticator
```
会自动通过securityManager.setAuthenticator(authenticator)注入引用依赖。

**嵌套属性setter注入**
```
securityManager.authenticator.authenticationStrategy=$authenticationStrategy
```

**byte数组setter注入**
```
#base64 byte[]
authenticator.bytes=aGVsbG8=
#hex byte[]
authenticator.bytes=0x68656c6c6f
```
默认需要使用Base64进行编码，也可以使用0x十六进制。

**Array/Set/List setter注入**
```
authenticator.array=1,2,3
authenticator.set=$jdbcRealm,$jdbcRealm
```
多个之间通过“，”分割。

**Map setter注入**
```
authenticator.map=$jdbcRealm:$jdbcRealm,1:1,key:abc
```
即格式是：map=key：value，key：value，可以注入常量及引用值，常量的话都看作字符串（即使有泛型也不会自动造型）。

**实例化/注入顺序**
```
realm=Realm1
realm=Realm12

authenticator.bytes=aGVsbG8=
authenticator.bytes=0x68656c6c6f
```
后边的覆盖前边的注入。

ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section4/src/test/resources/shiro-main.ini)

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section4/src/test/java/IniMainTest.java)

**[users]部分**

配置用户名/密码及其角色，格式：“用户名=密码，角色1，角色2”，角色部分可省略。如：
```
[users]
zhang=123,role1,role2
wang=123
```
密码一般生成其摘要/加密存储，后续章节介绍。

**[roles]部分**

配置角色及权限之间的关系，格式：“角色=权限1，权限2”；如：
```
[roles]
role1=user:create,user:update
role2=*
```
如果只有角色没有对应的权限，可以不配roles，具体规则请参考授权章节。

**[urls]部分**

配置url及相应的拦截器之间的关系，格式：“url=拦截器[参数]，拦截器[参数]，如：
```
[urls]
/admin/** = authc, roles[admin], perms["permission1"]
```
具体规则参见web相关章节。
