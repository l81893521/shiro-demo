package realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * Created by Will.Zhang on 2016/12/5 0005 16:00.
 */
public class MyRealm2 extends AuthorizingRealm{
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = "liu";
        //随机数
        String salt2 = "0072273a5d87322163795118fdd7c45e";
        //加密后的密码
        String password = "be320beca57748ab9632c4121ccac0db";

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(username, password, getName());
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(username + salt2));
        return authenticationInfo;
    }
}
