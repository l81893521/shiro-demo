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
public class MyRealm2 implements Realm{

	public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String) token.getPrincipal();
		String password = new String((char[])token.getCredentials());
		if(!"wang".equals(username)){
			//用户名错误
			throw new UnknownAccountException();
		}
		if(!"123".equals(password)){
			//密码错误
			throw new IncorrectCredentialsException();
		}
		//认证成功 返回一个Authentication的实现
		return new SimpleAuthenticationInfo(username, password, getName());
	}

	public String getName() {
		return "myRealm2";
	}

	public boolean supports(AuthenticationToken token) {
		// 仅支持usernamePasswordToken
		return token instanceof UsernamePasswordToken;
	}

}
