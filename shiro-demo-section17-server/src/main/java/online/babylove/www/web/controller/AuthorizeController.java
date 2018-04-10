package online.babylove.www.web.controller;

import online.babylove.www.service.ClientService;
import online.babylove.www.utils.Constants;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import online.babylove.www.service.OAuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Will.Zhang on 2017/4/17 0017 17:51.
 * 授权控制器
 */
@Controller
public class AuthorizeController {

    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private ClientService clientService;

    @RequestMapping("/authorize")
    public Object authorize(Model model, HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
        try {
            //构建OAuth授权请求
            OAuthAuthzRequest oAuthAuthzRequest = new OAuthAuthzRequest(request);
            //检查传入的客户端id是否正确
            if(!oAuthService.checkClientId(oAuthAuthzRequest.getClientId())){
                //客户端id不存在,返回错误
                OAuthResponse response = OAuthASResponse
                        .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                        .setErrorDescription(Constants.INVALID_CLIENT_DESCRIPTION)
                        .buildJSONMessage();
                return new ResponseEntity<String>(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }

            Subject subject = SecurityUtils.getSubject();
            //如果当前用户没有登录
            if(!subject.isAuthenticated()){
                //登录失败时跳转到登录页面
                if(!login(subject, request)){
                    model.addAttribute("client", clientService.findByClientId(oAuthAuthzRequest.getClientId()));
                    return "/oauth2login";
                }
            }

            String username = (String)subject.getPrincipal();
            //生成授权码
            String authorizationCode = null;
            //responseType目前仅支持code, 另外还有token
            String responseType = oAuthAuthzRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
            if(responseType.equals(ResponseType.CODE.toString())){
                OAuthIssuerImpl oAuthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
                authorizationCode = oAuthIssuerImpl.authorizationCode();
                oAuthService.addAuthCode(authorizationCode, username);
            }

            //进行OAuth响应构建
            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
            //设置授权码
            builder.setCode(authorizationCode);
            //得到客户端重定向地址
            String redirectURI = oAuthAuthzRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            //构建响应
            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();

            //根据OAuthResponse返回ResponseEntity响应
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(response.getLocationUri()));
            return new ResponseEntity<String>(headers, HttpStatus.valueOf(response.getResponseStatus()));

        } catch (OAuthProblemException e){
            //出错处理
            String redirectUri = e.getRedirectUri();
            if(OAuthUtils.isEmpty(redirectUri)){
                //告诉客户端没有传入uri直接报错
                return new ResponseEntity("OAuth callback url needs to be provided by client!!!", HttpStatus.NOT_FOUND);
            }

            //返回错误消息（如?error=）
            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(redirectUri)
                    .buildQueryMessage();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(redirectUri));
            return new ResponseEntity(headers, HttpStatus.valueOf(response.getResponseStatus()));
        }
    }

    /**
     * 简单登录
     * @param subject
     * @param request
     * @return
     */
    private boolean login(Subject subject, HttpServletRequest request){
        if("get".equalsIgnoreCase(request.getMethod())){
            return false;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return false;
        }

        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            subject.login(token);
            return true;
        } catch (Exception e) {
            request.setAttribute("error", "登录失败:" + e.getClass().getName());
            return false;
        }
    }
}
