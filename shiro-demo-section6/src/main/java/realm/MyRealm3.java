package realm;

import entity.User;
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.Realm;

/**
 * Created by Will.Zhang on 2017/2/7 0007 18:25.
 */
public class MyRealm3 implements Realm{
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
}
