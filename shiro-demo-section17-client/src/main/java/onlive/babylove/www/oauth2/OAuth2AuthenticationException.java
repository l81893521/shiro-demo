package onlive.babylove.www.oauth2;


import org.apache.shiro.authc.AuthenticationException;

/**
 * Created by Will.Zhang on 2017/4/21 0021 16:10.
 */
public class OAuth2AuthenticationException extends AuthenticationException {


    public OAuth2AuthenticationException(Throwable cause) {
        super(cause);
    }
}
