import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;

/**
 * Created by Will.Zhang on 2016/11/22 0022 16:09.
 */
public class BaseTest {

    @After
    public void tearDown() throws Exception {
        ThreadContext.unbindSubject();
    }

    protected void login(String configFile, String username, String password){
        //1.获取SecurityManagerFactory,此处用shiro.ini来初始化(使用自定义realm)
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(configFile);
        //2.得到securityManager实例
        SecurityManager securityManager = factory.getInstance();
        //3.绑定给securityManager
        SecurityUtils.setSecurityManager(securityManager);
        //4.获取subject
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken(username, password);

        subject.login(token);
    }

    public Subject getSubject(){
        return SecurityUtils.getSubject();
    }


}
