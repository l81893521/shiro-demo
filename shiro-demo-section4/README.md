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
等价的ini配置 [查看代码]()
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
测试用例 [查看代码]()
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