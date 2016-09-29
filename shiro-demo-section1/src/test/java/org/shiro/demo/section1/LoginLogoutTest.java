package org.shiro.demo.section1;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Assert;
import org.junit.Test;


public class LoginLogoutTest {
	
	@Test
	public void testHelloworld(){
		//1.获取SecurityManagerFactory,此处用shiro.ini来初始化
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
		//2.得到securityManager实例
		SecurityManager securityManager = factory.getInstance();
		//3.绑定给securityManager
		SecurityUtils.setSecurityManager(securityManager);
		//4.获取subject
		Subject subject = SecurityUtils.getSubject();
		
		UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
		
		try {
			//5.登录
			subject.login(token);
		} catch (Exception e) {
			//6.身份验证失败
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//7.登出
		subject.logout();
	}
	
	@Test
	public void testCustomRealm(){
		//1.获取SecurityManagerFactory,此处用shiro.ini来初始化(使用自定义realm)
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-realm.ini");
		//2.得到securityManager实例
		SecurityManager securityManager = factory.getInstance();
		//3.绑定给securityManager
		SecurityUtils.setSecurityManager(securityManager);
		//4.获取subject
		Subject subject = SecurityUtils.getSubject();
		
		UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
		
		try {
			//5.登录
			subject.login(token);
		} catch (Exception e) {
			//6.身份验证失败
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//7.登出
		subject.logout();
	}
	
	@Test
	public void testCustomMultiRealm(){
		//1.获取SecurityManagerFactory,此处用shiro.ini来初始化(使用自定义realm)
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-multi-realm.ini");
		//2.得到securityManager实例
		SecurityManager securityManager = factory.getInstance();
		//3.绑定给securityManager
		SecurityUtils.setSecurityManager(securityManager);
		//4.获取subject
		Subject subject = SecurityUtils.getSubject();
		
		UsernamePasswordToken token = new UsernamePasswordToken("wang", "123");
		
		try {
			//5.登录
			subject.login(token);
		} catch (Exception e) {
			//6.身份验证失败
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//7.登出
		subject.logout();
	}
	
	@Test
	public void testJDBCRealm(){
		//1.获取SecurityManagerFactory,此处用shiro.ini来初始化(使用自定义realm)
		Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro-jdbc-realm.ini");
		//2.得到securityManager实例
		SecurityManager securityManager = factory.getInstance();
		//3.绑定给securityManager
		SecurityUtils.setSecurityManager(securityManager);
		//4.获取subject
		Subject subject = SecurityUtils.getSubject();
		
		UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
		try {
			//5.登录
			subject.login(token);
		} catch (Exception e) {
			//6.身份验证失败
		}
		
		Assert.assertEquals(true, subject.isAuthenticated());
		
		//7.登出
		subject.logout();
	}
}
