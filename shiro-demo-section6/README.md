【2.5 Realm】及【3.5 Authorizer】部分都已经详细介绍过Realm了，接下来再来看一下一般真实环境下的Realm如何实现。

###6.1 Realm

1. 定义实体及关系

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/1.png)

即用户-角色之间是多对多关系，角色-权限之间是多对多关系；且用户和权限之间通过角色建立关系；
在系统中验证时通过权限验证，角色只是权限集合，即所谓的显式角色；
其实权限应该对应到资源（如菜单、URL、页面按钮、Java方法等）中，即应该将权限字符串存储到资源实体中，但是目前为了简单化，直接提取一个权限表，【综合示例】部分会使用完整的表结构。

用户实体包括：编号(id)、用户名(username)、密码(password)、盐(salt)、是否锁定(locked)；是否锁定用于封禁用户使用，其实最好使用Enum字段存储，可以实现更复杂的用户状态实现。

角色实体包括：、编号(id)、角色标识符（role）、描述（description）、是否可用（available）；
其中角色标识符用于在程序中进行隐式角色判断的，描述用于以后再前台界面显示的、是否可用表示角色当前是否激活。

权限实体包括：编号（id）、权限标识符（permission）、描述（description）、是否可用（available）；含义和角色实体类似不再阐述。

另外还有两个关系实体：

用户-角色实体（用户编号、角色编号，且组合为复合主键）；

角色-权限实体（角色编号、权限编号，且组合为复合主键）。

sql语句[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/sql/shiro.sql)

实体代码[查看代码](https://github.com/l81893521/shiro-demo/tree/master/shiro-demo-section6/src/main/java/entity)

2. 环境准备

为了方便数据库操作，使用了“org.springframework:spring-jdbc:4.3.5.RELEASE”依赖

3. 定义Service及Dao

为了实现的简单性，只实现必须的功能，其他的可以自己实现即可。

dao:[查看代码](https://github.com/l81893521/shiro-demo/tree/master/shiro-demo-section6/src/main/java/dao)

service:[查看代码](https://github.com/l81893521/shiro-demo/tree/master/shiro-demo-section6/src/main/java/service)

PasswordHelper:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/main/java/service/PasswordHelper.java)

之后的CredentialsMatcher需要和PasswordHelper加密的算法一样。user.getCredentialsSalt()辅助方法返回username+salt

测试用例:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/test/java/service/ServiceTest.java)

4. 定义Realm

RetryLimitHashedCredentialsMatcher:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/main/java/credentials/RetryLimitHashedCredentialsMatcher.java)

```
/**
     * 主要用于鉴权
     * @param principals
     * @return
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String)principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(userService.findRoles(username));
        authorizationInfo.setStringPermissions(userService.findPermissions(username));

        return authorizationInfo;
    }

    /**
     * 主要用于验证
     * @param token
     * @return
     * @throws AuthenticationException
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        String username = (String)token.getPrincipal();

        User user = userService.findByUsername(username);

        if(user == null) {
            throw new UnknownAccountException();//没找到帐号
        }

        if(Boolean.TRUE.equals(user.getLocked())) {
            throw new LockedAccountException(); //帐号锁定
        }

        //交给AuthenticatingRealm使用CredentialsMatcher进行密码匹配，如果觉得人家的不好可以自定义实现
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                user.getUsername(), //用户名
                user.getPassword(), //密码
                ByteSource.Util.bytes(user.getCredentialsSalt()),//salt=username+salt
                getName()  //realm name
        );
        return authenticationInfo;
    }
```

UserRealm:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/main/java/realm/UserRealm.java)

1. **UserRealm父类AuthorizingRealm**将获取Subject相关信息分成两步:
    * 获取身份验证信息（doGetAuthenticationInfo）
    * 授权信息（doGetAuthorizationInfo）

2. **doGetAuthenticationInfo**获取身份验证相关信息 :
    * 首先根据传入的用户名获取User信息
    * 然后如果user为空，那么抛出没找到帐号异常UnknownAccountException
    * 如果user找到但锁定了抛出锁定异常LockedAccountException
    * 最后生成AuthenticationInfo信息，交给间接父类AuthenticatingRealm使用CredentialsMatcher进行判断密码是否匹配，如果不匹配将抛出密码错误异常IncorrectCredentialsException
    * 另外如果密码重试此处太多将抛出超出重试次数异常ExcessiveAttemptsException

在组装SimpleAuthenticationInfo信息时，需要传入：身份信息（用户名）、凭据（密文密码）、盐（username+salt），CredentialsMatcher使用盐加密传入的明文密码和此处的密文密码进行匹配。

3. **doGetAuthorizationInfo**获取授权信息：PrincipalCollection是一个身份集合，因为我们现在就一个Realm，所以直接调用getPrimaryPrincipal得到之前传入的用户名即可；然后根据用户名调用UserService接口获取角色及权限信息。

测试用例:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/test/java/realm/UserRealmTest.java)

###6.2 AuthenticationToken

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/2.png)

AuthenticationToken用于收集用户提交的身份（如用户名）及凭据（如密码）：

```
public interface AuthenticationToken extends Serializable {
    Object getPrincipal(); //身份
    Object getCredentials(); //凭据
}
```
扩展接口RememberMeAuthenticationToken：提供了“boolean isRememberMe()”现“记住我”的功能；

扩展接口是HostAuthenticationToken：提供了“String getHost()”方法用于获取用户“主机”的功能。

Shiro提供了一个直接拿来用的UsernamePasswordToken，用于实现用户名/密码Token组，另外其实现了RememberMeAuthenticationToken和HostAuthenticationToken，可以实现记住我及主机验证的支持。

###6.3 AuthenticationInfo

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/3.png)

AuthenticationInfo有两个作用：
1. 如果Realm是AuthenticatingRealm子类，则提供给AuthenticatingRealm内部使用的CredentialsMatcher进行凭据验证；（如果没有继承它需要在自己的Realm中自己实现验证）；

2. 提供给SecurityManager来创建Subject（提供身份信息）；

MergableAuthenticationInfo用于提供在多Realm时合并AuthenticationInfo的功能，主要合并Principal、如果是其他的如credentialsSalt，会用后边的信息覆盖前边的。

比如HashedCredentialsMatcher，在验证时会判断AuthenticationInfo是否是SaltedAuthenticationInfo子类，来获取盐信息。

Account相当于我们之前的User，SimpleAccount是其一个实现；
在IniRealm、PropertiesRealm这种静态创建帐号信息的场景中使用，这些Realm直接继承了SimpleAccountRealm，而SimpleAccountRealm提供了相关的API来动态维护SimpleAccount；
即可以通过这些API来动态增删改查SimpleAccount；动态增删改查角色/权限信息。
及如果您的帐号不是特别多，可以使用这种方式，具体请参考SimpleAccountRealm Javadoc。

其他情况一般返回SimpleAuthenticationInfo即可。

###6.4 PrincipalCollection

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/4.png)

因为我们可以在Shiro中同时配置多个Realm，所以身份信息可能就有多个；因此其提供了PrincipalCollection用于聚合这些身份信息

```
public interface PrincipalCollection extends Iterable, Serializable {
    Object getPrimaryPrincipal(); //得到主要的身份
    <T> T oneByType(Class<T> type); //根据身份类型获取第一个
    <T> Collection<T> byType(Class<T> type); //根据身份类型获取一组
    List asList(); //转换为List
    Set asSet(); //转换为Set
    Collection fromRealm(String realmName); //根据Realm名字获取
    Set<String> getRealmNames(); //获取所有身份验证通过的Realm名字
    boolean isEmpty(); //判断是否为空
}
```

因为PrincipalCollection聚合了多个，此处最需要注意的是getPrimaryPrincipal，如果只有一个Principal那么直接返回即可，如果有多个Principal，则返回第一个（因为内部使用Map存储，所以可以认为是返回任意一个）；

oneByType / byType根据凭据的类型返回相应的Principal；fromRealm根据Realm名字（每个Principal都与一个Realm关联）获取相应的Principal。

MutablePrincipalCollection是一个可变的PrincipalCollection接口，即提供了如下可变方法：

```
public interface MutablePrincipalCollection extends PrincipalCollection {
    void add(Object principal, String realmName); //添加Realm-Principal的关联
    void addAll(Collection principals, String realmName); //添加一组Realm-Principal的关联
    void addAll(PrincipalCollection principals);//添加PrincipalCollection
    void clear();//清空
}
```

目前Shiro只提供了一个实现SimplePrincipalCollection，还记得之前的AuthenticationStrategy实现嘛，
用于在多Realm时判断是否满足条件的，在大多数实现中（继承了AbstractAuthenticationStrategy）afterAttempt方法会进行AuthenticationInfo（实现了MergableAuthenticationInfo）的merge，
比如SimpleAuthenticationInfo会合并多个Principal为一个PrincipalCollection。

接下来通过示例来看看PrincipalCollection。

1. 准备3个realm

myRealm1
```
public String getName() {
        //realm的名字为a
        return "a";
    }

    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(
                "zhang", //身份 字符串类型
                "123",   //凭据
                getName() //Realm Name
        );
    }
```

myRealm2(和myRealm1是一样的)
```
public String getName() {
        //realm的名字为b
        return "b";
    }

    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(
                "zhang", //身份 字符串类型
                "123",   //凭据
                getName() //Realm Name
        );
    }
```

myRealm3(principal为user类型)

```
public String getName() {
        //realm name 为 “c”
        return "c";
    }

    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        User user = new User("zhang", "123");
        return new SimpleAuthenticationInfo(
                user, //身份 User类型
                "123",   //凭据
                getName() //Realm Name
        );
    }
```

[查看代码](https://github.com/l81893521/shiro-demo/tree/master/shiro-demo-section6/src/main/java/realm)

2. shiro-multirealm.ini [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/main/resources/shiro-multirealm.ini)

3. 测试用例:
```
//因为Realm里没有进行验证，所以相当于每个Realm都身份验证成功了
        login("classpath:shiro-multirealm.ini", "zhang", "123");
        Subject subject = getSubject();
        //获取Principal
        Object primaryPrincipal1 = subject.getPrincipal();
        PrincipalCollection princialCollection = subject.getPrincipals();
        Object primaryPrincipal2 = princialCollection.getPrimaryPrincipal();
        //但是因为多个Realm都返回了Principal，所以此处到底是哪个是不确定的
        Assert.assertEquals(primaryPrincipal1, primaryPrincipal2);

        //返回 a b c
        Set<String> realmNames = princialCollection.getRealmNames();
        System.out.println(realmNames);

        //因为MyRealm1和MyRealm2返回的凭据都是zhang，所以排重了
        Set<Object> principals = princialCollection.asSet(); //asList和asSet的结果一样
        System.out.println(principals);

        //根据Realm名字获取
        Collection<User> users = princialCollection.fromRealm("c");
        System.out.println(users);
```
[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/src/test/java/realm/PrincialCollectionTest.java)

因为我们的Realm中没有进行身份及凭据验证，所以相当于身份验证都是成功的，都将返回
```
Object primaryPrincipal1 = subject.getPrincipal();
PrincipalCollection princialCollection = subject.getPrincipals();
Object primaryPrincipal2 = princialCollection.getPrimaryPrincipal();
```
我们可以直接调用subject.getPrincipal获取PrimaryPrincipal（即所谓的第一个）；
或者通过getPrincipals获取PrincipalCollection；
然后通过其getPrimaryPrincipal获取PrimaryPrincipal。

```
Set<String> realmNames = princialCollection.getRealmNames();
```
获取所有身份验证成功的Realm名字。

```
Set<Object> principals = princialCollection.asSet();
```
将身份信息转换为Set/List，即使转换为List，也是先转换为Set再完成的

```
Collection<User> users = princialCollection.fromRealm("c");
```
根据Realm名字获取身份，因为Realm名字可以重复，所以可能多个身份，建议Realm名字尽量不要重复。

###6.5 AuthorizationInfo

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/5.png)
AuthorizationInfo用于聚合授权信息的：
```
public interface AuthorizationInfo extends Serializable {
    Collection<String> getRoles(); //获取角色字符串信息
    Collection<String> getStringPermissions(); //获取权限字符串信息
    Collection<Permission> getObjectPermissions(); //获取Permission对象信息
}
```
当我们使用AuthorizingRealm时，如果身份验证成功，在进行授权时就通过doGetAuthorizationInfo方法获取角色/权限信息用于授权验证。

Shiro提供了一个实现SimpleAuthorizationInfo，大多数时候使用这个即可。

对于Account及SimpleAccount，之前的【6.3 AuthenticationInfo】已经介绍过了，用于SimpleAccountRealm子类，实现动态角色/权限维护的。

###6.6 Subject
![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section6/images/6.png)

Subject是Shiro的核心对象，基本所有身份验证、授权都是通过Subject完成。

* 身份信息获取
```
Object getPrincipal(); //Primary Principal
PrincipalCollection getPrincipals(); // PrincipalCollection
```

* 身份验证
```
void login(AuthenticationToken token) throws AuthenticationException;
boolean isAuthenticated();
boolean isRemembered();
```
通过login登录，如果登录失败将抛出相应的AuthenticationException，如果登录成功调用isAuthenticated就会返回true，
即已经通过身份验证；如果isRemembered返回true，表示是通过记住我功能登录的而不是调用login方法登录的。
isAuthenticated/isRemembered是互斥的，即如果其中一个返回true，另一个返回false。

* 角色授权验证
```
boolean hasRole(String roleIdentifier);
boolean[] hasRoles(List<String> roleIdentifiers);
boolean hasAllRoles(Collection<String> roleIdentifiers);
void checkRole(String roleIdentifier) throws AuthorizationException;
void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException;
void checkRoles(String... roleIdentifiers) throws AuthorizationException;
```
hasRole*进行角色验证，验证后返回true/false；而checkRole*验证失败时抛出AuthorizationException异常。

* 权限授权验证
```
boolean isPermitted(String permission);
boolean isPermitted(Permission permission);
boolean[] isPermitted(String... permissions);
boolean[] isPermitted(List<Permission> permissions);
boolean isPermittedAll(String... permissions);
boolean isPermittedAll(Collection<Permission> permissions);
void checkPermission(String permission) throws AuthorizationException;
void checkPermission(Permission permission) throws AuthorizationException;
void checkPermissions(String... permissions) throws AuthorizationException;
void checkPermissions(Collection<Permission> permissions) throws AuthorizationException;
```
isPermitted*进行权限验证，验证后返回true/false；而checkPermission*验证失败时抛出AuthorizationException。

* 会话
```
Session getSession(); //相当于getSession(true)
Session getSession(boolean create);
```
类似于Web中的会话。如果登录成功就相当于建立了会话，接着可以使用getSession获取；
如果create=false如果没有会话将返回null，而create=true如果没有会话会强制创建一个。

* 退出
```
void logout();
```

* RunAs
```
void runAs(PrincipalCollection principals) throws NullPointerException, IllegalStateException;
boolean isRunAs();
PrincipalCollection getPreviousPrincipals();
PrincipalCollection releaseRunAs();
```
RunAs即实现“允许A假设为B身份进行访问”；通过调用subject.runAs(b)进行访问；
接着调用subject.getPrincipals将获取到B的身份；此时调用isRunAs将返回true；
而a的身份需要通过subject. getPreviousPrincipals获取；如果不需要RunAs了调用subject. releaseRunAs即可。

* 多线程
```
<V> V execute(Callable<V> callable) throws ExecutionException;
void execute(Runnable runnable);
<V> Callable<V> associateWith(Callable<V> callable);
Runnable associateWith(Runnable runnable);
```
实现线程之间的Subject传播，因为Subject是线程绑定的；
因此在多线程执行中需要传播到相应的线程才能获取到相应的Subject。
最简单的办法就是通过execute(runnable/callable实例)直接调用；
或者通过associateWith(runnable/callable实例)得到一个包装后的实例；
它们都是通过：1、把当前线程的Subject绑定过去；2、在线程执行结束后自动释放。

Subject自己不会实现相应的身份验证/授权逻辑，而是通过DelegatingSubject委托给SecurityManager实现；
及可以理解为Subject是一个面门。

对于Subject的构建一般没必要我们去创建；一般通过SecurityUtils.getSubject()获取：
```
public static Subject getSubject() {
    Subject subject = ThreadContext.getSubject();
    if (subject == null) {
        subject = (new Subject.Builder()).buildSubject();
        ThreadContext.bind(subject);
    }
    return subject;
}
```
即首先查看当前线程是否绑定了Subject，如果没有通过Subject.Builder构建一个然后绑定到现场返回。

如果想自定义创建，可以通过：
```
new Subject.Builder().principals(身份).authenticated(true/false).buildSubject()
```
这种可以创建相应的Subject实例了，然后自己绑定到线程即可。
在new Builder()时如果没有传入SecurityManager，自动调用SecurityUtils.getSecurityManager获取；
也可以自己传入一个实例。

对于Subject我们一般这么使用：
1. 身份验证（login）

2. 授权（hasRole*/isPermitted*或checkRole*/checkPermission*）

3. 将相应的数据存储到会话（Session）

4. 切换身份（RunAs）/多线程身份传播

5. 退出