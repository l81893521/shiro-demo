import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Will.Zhang on 2016/11/22 0022 16:20.
 */
public class PermissionTest extends BaseTest {

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

    @Test
    public void testWPermission1(){
        login("classpath:shiro-permission.ini", "li", "123");

        getSubject().checkPermissions("system:user:update","system:user:delete");
        getSubject().checkPermissions("system:user:update,delete");
    }

    @Test
    public void testWPermission2(){
        login("classpath:shiro-permission.ini", "li", "123");

        getSubject().checkPermissions("system:user:create,update,delete,view");
        getSubject().checkPermissions("system:user:*");
        getSubject().checkPermissions("system:user");
    }

    @Test
    public void testWPermission3(){
        login("classpath:shiro-permission.ini", "li", "123");

        getSubject().checkPermissions("user:view");
        getSubject().checkPermissions("system:user:view");
    }

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

    @Test
    public void testWPermission5(){
        login("classpath:shiro-permission.ini", "li", "123");
        getSubject().checkPermissions("eat");
        getSubject().checkPermissions("eat:chicken");
        getSubject().checkPermissions("eat:chicken:wing");
    }

    @Test
    public void testWPermission6(){
        login("classpath:shiro-permission.ini", "li", "123");
        getSubject().checkPermission("eat");
        getSubject().checkPermission(new WildcardPermission("eat"));
    }
}
