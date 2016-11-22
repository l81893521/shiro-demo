# shrio-example
感谢开源教程 : [https://github.com/zhangkaitao/shiro-example](https://github.com/zhangkaitao/shiro-example)

## 1.1 简介
Apache shiro是一个java安全框架. 目前, 使用Apache shiro的人越来越多, 因为它相当简单, 对比spring security, 可能没有Spring Security功能强大,
但是在实际工作时可能并不需要那么复杂的东西,所以使用小而简单的shiro就足够了.

Shiro可以非常容易的开发出足够好的应用,其不仅可用在JavaSE环境,也可以用在JavaEE环境. Shiro可以帮助我们完成:认证、授权、加密、会话管理
、与web集成、缓存等.其基本功能如下图所示
![](https://github.com/l81893521/shiro-demo/blob/master/images/1.png)

**Authentication:** 身份认证/登录,验证用户是不是拥有相应的身份

**Authorization:** 授权,即权限验证,验证某个已认证的用户是否拥有某个权限;即判断用户是否能做的事情,常见的如:验证某个用户是否拥有
某个角色.或者细粒度的验证某个用户对某个资源是否具有某个权限

**Session Manager:** 会话管理,即用户登陆后就是一次会话, 在没有退出之前, 它的所有信息都在会话中;会话可以是普通JavaSE环境的,也可以是如Web环境的

**Cryptography:** 加密,保护数据的安全性,如密码加密存储到数据库,而不是明文存储

**Web Support:** Web支持，可以非常容易的集成到Web环境

**Caching:** 缓存，比如用户登录后，其用户信息、拥有的角色/权限不必每次去查，这样可以提高效率

**Concurrency:** shiro支持多线程应用并发验证，即如在一个线程中开启另一个线程，能把权限自动传播过去

**Testing:** 提供测试支持

**Run as:** 允许一个用户假装为另一个用户（如果他们允许）的身份进行访问

**Remember me:** 记住我，这个是非常常见的功能，即一次登录后，下次再来的话不用登录了

**记住一点,Shiro不会去维护用户,维护权限;这些需要我们自己去设计;然后通过相应的接口注入给Shiro**

接下来我们分别从外部和内部来看看Shiro的架构，对于一个好的框架，
从外部来看应该具有非常简单易于使用的API，且API契约明确；从内部来看的话，其应该有一个可扩展的架构，
即非常容易插入用户自定义实现，因为任何框架都不能满足所有需求。

首先，我们从外部来看Shiro吧，即从应用程序角度的来观察如何使用Shiro完成工作。如下图：
![](https://github.com/l81893521/shiro-demo/blob/master/images/2.png)
