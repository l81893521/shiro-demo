package online.babylove.www.web.controller;

import online.babylove.www.service.OAuthService;
import online.babylove.www.utils.Constants;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Will.Zhang on 2017/4/20 0020 18:27.
 */
@RestController
public class UserInfoController {

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping("/userInfo")
    public HttpEntity userInfo(HttpServletRequest request) throws OAuthSystemException {
        try {
            //构建OAuth资源请求
            OAuthAccessResourceRequest resourceRequest = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
            //获取access token
            String accessToken = resourceRequest.getAccessToken();
            //验证token
            if(!oAuthService.checkAccessToken(accessToken)){
                //如果不存在/过期, 返回未验证错误,需重新验证
                OAuthResponse response = OAuthResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setRealm(Constants.RESOURCE_SERVER_NAME)
                        .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
                        .buildHeaderMessage();

                HttpHeaders headers = new HttpHeaders();
                headers.add(OAuth.HeaderType.WWW_AUTHENTICATE, response.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
                return new ResponseEntity(headers, HttpStatus.OK);
            }
            //返回用户名
            String username = oAuthService.getUsernameByAccessToken(accessToken);
            return new ResponseEntity(username, HttpStatus.OK);

        } catch (OAuthProblemException e) {
            //检查是否设置了错误码
            String errorCode = e.getError();
            if(OAuthUtils.isEmpty(errorCode)){
                OAuthResponse response = OAuthResponse
                        .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                        .setRealm(Constants.RESOURCE_SERVER_NAME)
                        .buildHeaderMessage();
            }

            OAuthResponse response = OAuthResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setRealm(Constants.RESOURCE_SERVER_NAME)
                    .setError(errorCode)
                    .setErrorDescription(e.getDescription())
                    .setErrorUri(e.getUri())
                    .buildHeaderMessage();

            HttpHeaders headers = new HttpHeaders();
            headers.add(OAuth.HeaderType.WWW_AUTHENTICATE, response.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }
}
