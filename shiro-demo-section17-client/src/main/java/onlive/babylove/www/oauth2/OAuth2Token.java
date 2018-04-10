package onlive.babylove.www.oauth2;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Created by Will.Zhang on 2017/4/20 0020 16:21.
 */
public class OAuth2Token implements AuthenticationToken{

    public OAuth2Token(String authCode) {
        this.authCode = authCode;
    }

    private String authCode;
    private String principal;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Object getCredentials() {
        return authCode;
    }
}
