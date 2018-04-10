package onlive.babylove.www.oauth2;

import com.alibaba.druid.util.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Will.Zhang on 2017/4/21 0021 12:26.
 */
public class OAuth2AuthenticationFilter extends AuthenticatingFilter{

    //oauth2 authc code参数名
    private String authCodeParam = "code";
    //客户端id
    private String clientId;
    //服务端登录成功/失败后重定向到的客户端地址
    private String redirectUrl;
    //oauth2服务器响应
    private String responseType = "code";

    private String failureUrl;

    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String code = request.getParameter(authCodeParam);
        return new OAuth2Token(code);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String error = servletRequest.getParameter("error");
        String errorDescription = servletRequest.getParameter("error_description");
        if(!StringUtils.isEmpty(error)){
            //如果服务端返回了错误
            WebUtils.issueRedirect(servletRequest, servletResponse, failureUrl + "?error=" + error + "error_description=" + errorDescription);
            return false;
        }

        Subject subject = getSubject(servletRequest, servletResponse);

        if(!subject.isAuthenticated()){
            if(StringUtils.isEmpty(servletRequest.getParameter(authCodeParam))){
                //如果用户没有身份验证，且没有auth code，则重定向到服务端授权
                saveRequestAndRedirectToLogin(servletRequest, servletResponse);
                return false;
            }
        }
        return executeLogin(servletRequest, servletResponse);
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        issueSuccessRedirect(request, response);
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException ae, ServletRequest request, ServletResponse response) {
        Subject subject = getSubject(request, response);
        if(subject.isAuthenticated() || subject.isRemembered()){
            try {
                issueSuccessRedirect(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                WebUtils.issueRedirect(request, response, failureUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public void setAuthCodeParam(String authCodeParam) {
        this.authCodeParam = authCodeParam;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public void setFailureUrl(String failureUrl) {
        this.failureUrl = failureUrl;
    }
}
