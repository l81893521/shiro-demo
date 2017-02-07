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