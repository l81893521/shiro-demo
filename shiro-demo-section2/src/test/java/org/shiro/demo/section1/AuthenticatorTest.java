package org.shiro.demo.section1;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author zhangjiawei
 *
 */
public class AuthenticatorTest {
	
	@Test
	public void testAllSuccessfulStrategyWithSuccess(){
		login("classpath:shiro-authenticator-all-success.ini","zhang","123");
		
		Subject subject = SecurityUtils.getSubject();
		//得到一个身份集合，其包含了Realm验证成功的身份信息
		PrincipalCollection principalCollection = subject.getPrincipals();
		Assert.assertEquals(2, principalCollection.asList().size());
	}
	
	@Test(expected = UnknownAccountException.class)
	public void testAllSuccessfulStrategyWithFail(){
		login("classpath:shiro-authenticator-all-fail.ini","zhang","123");
	}
	
	@Test
	public void testAtLeastOneSuccessfulStrategyWithSuccess(){
		login("classpath:shiro-authenticator-atLeastOne-success.ini","zhang","123");
		
		Subject subject = SecurityUtils.getSubject();
		//得到一个身份集合，其包含了Realm验证成功的身份信息
		PrincipalCollection principalCollection = subject.getPrincipals();
		Assert.assertEquals(2, principalCollection.asList().size());
	}
	
	@Test
	public void testFirstOneSuccessfulStrategyWithSuccess(){
		login("classpath:shiro-authenticator-first-success.ini","zhang","123");
		
		Subject subject = SecurityUtils.getSubject();
		//得到一个身份集合，其包含了Realm验证成功的身份信息
		PrincipalCollection principalCollection = subject.getPrincipals();
		Assert.assertEquals(1, principalCollection.asList().size());
	}
	
	@Test
	public void testAtLeastTwoStrategyWithSuccess(){
		login("classpath:shiro-authenticator-atLeastTwo-success.ini","zhang","123");
		
		Subject subject = SecurityUtils.getSubject();
		//得到一个身份集合，其包含了Realm1和Realm3的验证成功的身份信息
		PrincipalCollection principalCollection = subject.getPrincipals();
		Assert.assertEquals(2, principalCollection.asList().size());
	}
	
	@Test
	public void testOnlyOneStrategyWithSuccess(){
		login("classpath:shiro-authenticator-onlyOne-success.ini","wang","123");
		
		Subject subject = SecurityUtils.getSubject();
		//得到一个身份集合，其包含了Realm验证成功的身份信息
		PrincipalCollection principalCollection = subject.getPrincipals();
		Assert.assertEquals(1, principalCollection.asList().size());
	}
	
	private void login(String configFile, String username, String password){
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
	
	@After
    public void tearDown() throws Exception {
        ThreadContext.unbindSubject();//退出时请解除绑定Subject到线程 否则对下次测试造成影响
    }
}
