package org.shiro.demo.section1.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;

/**
 * 
 * @author zhangjiawei
 *
 */
public class MyRealm3 implements Realm{

	@Override
	public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String) token.getPrincipal();
		String password = new String((char[])token.getCredentials());
		if(!"zhang".equals(username)){
			//用户名错误
			throw new UnknownAccountException();
		}
		if(!"123".equals(password)){
			//密码错误
			throw new IncorrectCredentialsException();
		}
		//认证成功 返回一个Authentication的实现
		return new SimpleAuthenticationInfo(username + "@163.com", password, getName());
	}

	@Override
	public String getName() {
		return "myRealm3";
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

}
