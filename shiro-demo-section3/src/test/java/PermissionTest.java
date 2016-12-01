import org.apache.shiro.authz.UnauthorizedException;
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
}
