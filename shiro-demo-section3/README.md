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

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/RoleTest.java)
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

Shiro提供了hasRole/hasAllRoles用于判断用户是否拥有某个角色/某些权限；
但是没有提供如hashAnyRole用于判断是否有某些权限中的某一个。

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/RoleTest.java)
```
@Test(expected = UnauthorizedException.class)
public void testCheckRole(){
    login("classpath:shiro-role.ini", "zhang", "123");
    //拥有角色 role1
    getSubject().checkRole("role1");
    //拥有角色 role1 and role3 抛出异常
    getSubject().checkRoles("role1", "role3");
}
```
Shiro提供的checkRole/checkRoles和hasRole/hasAllRoles不同的地方是它在判断为假的情况下会抛出UnauthorizedException异常。

**基于资源的访问控制（显式角色）**

1、在shiro-permission.ini配置文件配置用户拥有的角色及角色-权限关系 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)
```
#用户名=密码，角色1，角色2
[users]
zhang=123,role1,role2
wang=123,role1
#角色=权限1，权限2
[roles]
role1=user:create,user:update
role2=user:create,user:delete
```
规则：“用户名=密码，角色1，角色2”“角色=权限1，权限2”，即首先根据用户名找到角色，然后根据角色再找到权限；
即角色是权限集合；Shiro同样不进行权限的维护，需要我们通过Realm返回相应的权限信息。
只需要维护“用户——角色”之间的关系即可。

测试用例:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testIsPermitted(){
    login("classpath:shiro-permission.ini", "zhang", "123");
    //判断是否拥有权限user:create
    Assert.assertTrue(getSubject().isPermitted("user:create"));
    //判断是否拥有权限user:create and user:delete
    Assert.assertTrue(getSubject().isPermittedAll("user:create", "user:delete"));
    //判断 没有权限 user:view
    Assert.assertFalse(getSubject().isPermitted("user:view"));
}
```

Shiro提供了isPermitted和isPermittedAll用于判断用户是否拥有某个权限或所有权限，
也没有提供如isPermittedAny用于判断拥有某一个权限的接口。

测试用例:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test(expected = UnauthorizedException.class)
public void testCheckPermission(){
    login("classpath:shiro-permission.ini", "zhang", "123");
    //拥有权限 user:create
    getSubject().checkPermission("user:create");
    //拥有权限 user:delete and user:update
    getSubject().checkPermissions("user:delete", "user:update");
    //拥有权限 user:view 抛出错误
    getSubject().checkPermission("user:view");

}
```
Shiro提供的checkPermission/checkPermissions和isPermitted/isPermittedAll不同的地方是它在判断为假的情况下会抛出UnauthorizedException异常。

到此基于资源的访问控制（显示角色）就完成了，也可以叫基于权限的访问控制，这种方式的一般规则是“资源标识符：操作”，即是资源级别的粒度；
这种方式的好处就是如果要修改基本都是一个资源级别的修改，不会对其他模块代码产生影响，粒度小。但是实现起来可能稍微复杂点，需要维护“用户——角色，角色——权限（资源：操作）”之间的关系。

###3.3 Permission

**字符串通配符权限**

规则：“资源标识符：操作：对象实例ID”  即对哪个资源的哪个实例可以进行什么操作。

其默认支持通配符权限字符串，“:”表示资源/操作/实例的分割；“,”表示操作的分割；“*”表示任意资源/操作/实例。

**单资源单权限**
```
subject().checkPermissions("system:user:update");
```

**单资源多权限**

ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)
```
#对资源user拥有update,delete权限
role41=system:user:update,system:user:delete
```
也可以简写成
```
#对资源user拥有update,delete权限,需要加双引号(简写,但不等价)
role42:"system:user:update,delete"
```
测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission1(){
    login("classpath:shiro-permission.ini", "li", "123");

    getSubject().checkPermissions("system:user:update","system:user:delete");
    getSubject().checkPermissions("system:user:update,delete");
}
```

**单资源全部权限**
ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)
```
#对资源user拥有create,update,delete,view权限
role51:"system:user:create,update,delete,view"
```
或者(推荐)
```
#对资源user拥有所有权限
role52:system:user:*
```
或者
```
#对资源user拥有所有权限
role53:system:user
```
测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission2(){
    login("classpath:shiro-permission.ini", "li", "123");

    getSubject().checkPermissions("system:user:create,update,delete,view");
    getSubject().checkPermissions("system:user:*");
    getSubject().checkPermissions("system:user");
}
```

**所有资源指定权限**
ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)
```
#对所有资源拥有view权限(如匹配user:view)
role61=*:view
#对所有资源拥有view权限(如匹配system:user:view)
role62=*:*:view
```
测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission3(){
    login("classpath:shiro-permission.ini", "li", "123");

    getSubject().checkPermissions("user:view");
    getSubject().checkPermissions("system:user:view");
}
```

**单个实例单个权限**
```
#对资源user的1实例拥有view权限
role71=user:view:1
```
**单个实例多个权限**
```
#对资源user的1实例拥有update,delete权限
role72="user:update,delete:1"
```
**单个实例所有权限**
```
#对资源user的1实例拥有所有权限
role73=user:*:1
```
**所有实例单个权限**
```
#对资源user的所有实例拥有auth权限
role74=user:auth:*
```
**所有实例所有权限**
```
#对资源user的所有实例拥有所有权限
role75=user:*:*
```
ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission4(){
    login("classpath:shiro-permission.ini", "li", "123");
    //单实例单权限
    getSubject().checkPermission("user:view:1");
    //单实例多权限
    getSubject().checkPermissions("user:update:1","user:delete:1");
    //单实例全部权限
    getSubject().checkPermissions("user:eat:1","user:drink:1","user:run:1");
    //多实例单权限
    getSubject().checkPermissions("user:auth:1","user:auth:2","user:auth:3");
    //多实例多权限
    getSubject().checkPermissions("user:anything:anyone","user:anything-1:anyone-1","user:anything-2:anyone-2");
}
```

**权限字符串缺失部分的处理**

如“user:view”等价于“user:view:*”；

而“organization”等价于“organization:*”或者“organization:*:*”。

可以这么理解，这种方式实现了前缀匹配。

另外如“user:*”可以匹配如“user:delete”

“user:delete”可以匹配如“user:delete:1”

“user:*:1”可以匹配如“user:view:1”

“user”可以匹配“user:view”或“user:view:1”等。

即*可以匹配所有，不加*可以进行前缀匹配；

但是如“*:view”不能匹配“system:user:view”，需要使用“*:*:view”，即后缀匹配必须指定前缀（多个冒号就需要多个*来匹配）。

ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/resources/shiro-permission.ini)
```
#可匹配eat, eat:*, eat:*:*
role81=eat
```
测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission5(){
    login("classpath:shiro-permission.ini", "li", "123");
    getSubject().checkPermissions("eat");
    getSubject().checkPermissions("eat:chicken");
    getSubject().checkPermissions("eat:chicken:wing");
}
```

**WildcardPermission**

以下两种方式是等价的 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section3/src/test/java/PermissionTest.java)
```
@Test
public void testWPermission6(){
    login("classpath:shiro-permission.ini", "li", "123");
    getSubject().checkPermission("eat");
    getSubject().checkPermission(new WildcardPermission("eat"));
}
```
