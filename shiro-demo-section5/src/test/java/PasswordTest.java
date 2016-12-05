import org.junit.Test;

/**
 * Created by Will.Zhang on 2016/12/5 0005 14:49.
 */
public class PasswordTest extends BaseTest{

    @Test
    public void testPasswordServiceWithMyrealm(){
        login("classpath:shiro-passwordService.ini","zhang","123");
    }
}
