授权，也叫访问控制，即在应用中控制谁能访问哪些资源（如访问页面/编辑数据/页面操作等）。
在授权中需了解的几个关键对象：主体（Subject）、资源（Resource）、权限（Permission）、角色（Role）。

* 主体(Subject):主体，即访问应用的用户，在Shiro中使用Subject代表该用户。用户只有授权后才允许访问相应的资源。
* 资源(Resource):在应用中用户可以访问的任何东西，比如访问JSP页面、查看/编辑某些数据、访问某个业务方法、打印文本等等都是资源。用户只要授权后才能访问。
* 权限(Permission):安全策略中的原子授权单位，通过权限我们可以表示在应用中用户有没有操作某个资源的权力。
即权限表示在应用中用户能不能访问某个资源如：访问用户列表页面,查看/新增/修改/删除用户数据

如上可以看出，权限代表了用户有没有操作某个资源的权利，即反映在某个资源上的操作允不允许，不反映谁去执行这个操作。
所以后续还需要把权限赋予给用户，即定义哪个用户允许在某个资源上做什么操作（权限），Shiro不会去做这件事情，而是由实现人员提供。

Shiro支持粗粒度权限（如用户模块的所有权限）和细粒度权限（操作某个用户的权限，即实例级别的），后续部分介绍。

* 角色:角色代表了操作集合，可以理解为权限的集合，一般情况下我们会赋予用户角色而不是权限，即这样用户可以拥有一组权限，赋予权限时比较方便。
典型的如：项目经理、技术总监、CTO、开发工程师等都是角色，不同的角色拥有一组不同的权限。
* 隐式角色:即直接通过角色来验证用户有没有操作权限，如在应用中CTO、技术总监、开发工程师可以使用打印机，假设某天不允许开发工程师使用打印机，此时需要从应用中删除相应代码；
再如在应用中CTO、技术总监可以查看用户、查看权限；突然有一天不允许技术总监查看用户、查看权限了，需要在相关代码中把技术总监角色从判断逻辑中删除掉
即粒度是以角色为单位进行访问控制的，粒度较粗；如果进行修改可能造成多处代码修改。
* 显式角色:在程序中通过权限控制谁能访问某个资源，角色聚合一组权限集合；这样假设哪个角色不能访问某个资源，只需要从角色代表的权限集合中移除即可；
无须修改多处代码；即粒度是以资源/实例为单位的；粒度较细。

### 3.1 授权方式
Shiro支持三种方式的授权：

编程式：通过写if/else授权代码块完成：
```
Subject subject = SecurityUtils.getSubject();
if(subject.hasRole(“admin”)) {
    //有权限
} else {
    //无权限
}
```
注解式：通过在执行的Java方法上放置相应的注解完成：
```
@RequiresRoles("admin")
public void hello() {
    //有权限
}
```
没有权限将抛出相应的异常；

JSP/GSP标签：在JSP/GSP页面通过相应的标签完成：
```
<shiro:hasRole name="admin">
<!— 有权限 —>
</shiro:hasRole>
```
后续部分将详细介绍如何使用。

### 3.2 授权
**基于角色的访问控制（隐式角色）**

shiro-role.ini配置文件配置用户拥有的角色 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-role.ini)
```
#用户名=密码,角色1,角色2
[users]
zhang=123,role1,role2
wang=123,role1
```

规则即：“用户名=密码,角色1,角色2”
如果需要在应用中判断用户是否有相应角色，就需要在相应的Realm中返回角色信息，
也就是说Shiro不负责维护用户-角色信息，需要应用提供，Shiro只是提供相应的接口方便验证，后续会介绍如何动态的获取用户角色。

测试用例 [查看代码]()
```
@Test
public void testHasRole(){
    login("classpath:shiro-role.ini", "zhang", "123");
    //判断是否拥有角色 role1
    Assert.assertTrue(getSubject().hasRole("role1"));
    //判断是否拥有角色 role1 和 role2
    Assert.assertTrue(getSubject().hasAllRoles(Arrays.asList("role1", "role2")));
    //判断是否拥有角色 role1 and role2 and !role3
    boolean[] result = getSubject().hasRoles(Arrays.asList("role1", "role2", "role3"));
    Assert.assertEquals(true, result[0]);
    Assert.assertEquals(true, result[1]);
    Assert.assertEquals(false, result[2]);
}
```