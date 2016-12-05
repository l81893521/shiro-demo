import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.Test;

/**
 * Created by Will.Zhang on 2016/12/5 0005 14:49.
 */
public class PasswordTest extends BaseTest{

    @Test
    public void testPasswordServiceWithMyrealm(){
        login("classpath:shiro-passwordService.ini","zhang","123");
    }

    @Test
    public void testPasswordServiceWithJdbcRealm(){
        login("classpath:shiro-jdbc-passwordService.ini","zhang","123");
    }

    @Test
    public void testGeneratePassword(){
        String algorithmName = "md5";
        String username = "liu";
        String password = "123";
        String salt1 = username;
        String salt2 = new SecureRandomNumberGenerator().nextBytes().toHex();
        int hashIteration = 2;

        SimpleHash simpleHash = new SimpleHash(algorithmName, password, salt1 + salt2, hashIteration);
        String encodePassword = simpleHash.toHex();
        System.out.println(salt2);
        System.out.println(encodePassword);
    }

    @Test
    public void testHashedCredentialsMatcherWithMyRealm2(){
        //使用testGeneratePassword生成的散列密码
        login("classpath:shiro-hashedCredentialsMatcher.ini", "liu", "123");
    }
}
