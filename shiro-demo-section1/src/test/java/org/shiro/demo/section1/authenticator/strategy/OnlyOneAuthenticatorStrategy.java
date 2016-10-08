package org.shiro.demo.section1.authenticator.strategy;

import java.util.Collection;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;

/**
 * 只允许一个realm通过验证
 * 自定义实现时一般继承org.apache.shiro.authc.pam.AbstractAuthenticationStrategy即可
 * @author zhangjiawei
 *
 */
public class OnlyOneAuthenticatorStrategy extends AbstractAuthenticationStrategy{
	
	/**
	 * 在所有Realm验证之前调用 
	 */
	@Override
	public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
		//返回一个权限的认证信息
		return new SimpleAuthenticationInfo();
	}
	
	/**
	 * 在每个Realm之前调用
	 */
	@Override
	public AuthenticationInfo beforeAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
		//返回之前合并的
		return aggregate;
	}
	
	/**
	 * 在每个Realm之后调用
	 */
	@Override
	public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
		AuthenticationInfo authenticationInfo = null;
		if(singleRealmInfo == null){//当前没有通过验证
			authenticationInfo = aggregateInfo;//保存之前所合并的
		}else{//通过验证
			if(aggregateInfo== null){//之前没有合并过
				authenticationInfo = singleRealmInfo;//初始化
			}else{
				authenticationInfo = merge(singleRealmInfo, aggregateInfo);//合并
				if(authenticationInfo.getPrincipals().getRealmNames().size() > 1){
					System.out.println(authenticationInfo.getPrincipals().getRealmNames());
                    throw new AuthenticationException("[" + token.getClass() + "] " +
                            "这个认证令牌无法通过realm的验证，请确认您提供的令牌只允许通过1个realm验证");
				}
			}
		}
		return authenticationInfo;
	}
	
	/**
	 * 在所有Realm之后调用
	 */
	@Override
	public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
		return aggregate;
	}
}
