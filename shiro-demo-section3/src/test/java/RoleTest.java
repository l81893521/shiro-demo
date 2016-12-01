import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Will.Zhang on 2016/11/22 0022 16:05.
 */
public class RoleTest extends BaseTest{

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

    @Test(expected = UnauthorizedException.class)
    public void testCheckRole(){
        login("classpath:shiro-role.ini", "zhang", "123");
        //拥有角色 role1
        getSubject().checkRole("role1");
        //拥有角色 role1 and role3 抛出异常
        getSubject().checkRoles("role1", "role3");
    }

}
