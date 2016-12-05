package realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Created by Will.Zhang on 2016/12/5 0005 14:36.
 */
public class MyRealm extends AuthorizingRealm{

    private PasswordService passwordService;

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //将credentialsMatcher赋值给myRealm，myRealm间接继承了AuthenticatingRealm，其在调用getAuthenticationInfo方法获取到AuthenticationInfo信息后，
        //会使用credentialsMatcher来验证凭据是否匹配，如果不匹配将抛出IncorrectCredentialsException异常。
        return new SimpleAuthenticationInfo("zhang", passwordService.encryptPassword("123"), getName());
    }
}
