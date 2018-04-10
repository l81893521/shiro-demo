###客户端

客户端流程：如果需要登录首先跳到oauth2服务端进行登录授权，成功后服务端返回auth code，然后客户端使用auth code去服务器端换取access token，
最后根据access token获取用户信息进行客户端的登录绑定。这个可以参照如很多网站的新浪微博登录功能，或其他的第三方帐号登录功能。

###POM依赖
此处我们使用apache oltu oauth2客户端实现。
```
<dependency>
    <groupId>org.apache.oltu.oauth2</groupId>
    <artifactId>org.apache.oltu.oauth2.client</artifactId>
    <version>1.0.2</version>
</dependency>
```

###OAuth2Token
类似于UsernamePasswordToken和CasToken；用于存储oauth2服务端返回的auth code。
```
public class OAuth2Token implements AuthenticationToken{

    public OAuth2Token(String authCode) {
        this.authCode = authCode;
    }

    private String authCode;
    private String principal;

    //省略getter setter
```

###OAuth2AuthenticationFilter
该filter的作用类似于FormAuthenticationFilter用于oauth2客户端的身份验证控制；
如果当前用户还没有身份验证，首先会判断url中是否有code（服务端返回的auth code）,如果没有则重定向到服务端进行登录并授权，然后返回auth code；
接着OAuth2AuthenticationFilter会用auth code创建OAuth2Token，然后提交给Subject.login进行登录；接着OAuth2Realm会根据OAuth2Token进行相应的登录逻辑。
```
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

    //省略getter and setter
}
```
该拦截器的作用：
1. 首先判断有没有服务端返回的error参数，如果有则直接重定向到失败页面；
2. 接着如果用户还没有身份验证，判断是否有auth code参数（即是不是服务端授权之后返回的），如果没有则重定向到服务端进行授权；
3. 否则调用executeLogin进行登录，通过auth code创建OAuth2Token提交给Subject进行登录；
4. 登录成功将回调onLoginSuccess方法重定向到成功页面；
5. 登录失败则回调onLoginFailure重定向到失败页面。

#OAuth2Realm
```
public class OAuth2Realm extends AuthorizingRealm{

    private String clientId;
    private String clientSecret;
    private String accessTokenUrl;
    private String userInfoUrl;
    private String redirectUrl;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;//表示此Realm只支持OAuth2Token类型
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        return authorizationInfo;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return null;
    }

    private String extractUsername(String code){
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation(accessTokenUrl)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setCode(code)
                    .setRedirectURI(redirectUrl)
                    .buildQueryMessage();

            OAuthAccessTokenResponse response = oAuthClient.accessToken(request, OAuth.HttpMethod.POST);

            String accessToken = response.getAccessToken();
            Long expiresIn = response.getExpiresIn();

            OAuthClientRequest userInfoRequest = new OAuthBearerClientRequest(userInfoUrl).setAccessToken(accessToken).buildQueryMessage();

            OAuthResourceResponse resourceResponse = oAuthClient.resource(userInfoRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

            String username = resourceResponse.getBody();
            return username;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OAuth2AuthenticationException(e);
        }
    }
}
```
此Realm首先只支持OAuth2Token类型的Token；
然后通过传入的auth code去换取access token；再根据access token去获取用户信息（用户名），然后根据此信息创建AuthenticationInfo；
如果需要AuthorizationInfo信息，可以根据此处获取的用户名再根据自己的业务规则去获取。

###Spring Shiro配置
```
<bean id="oAuth2Realm" class="onlive.babylove.www.oauth2.OAuth2Realm">
    <property name="cachingEnabled" value="true"/>
    <property name="authenticationCachingEnabled" value="true"/>
    <property name="authenticationCacheName" value="authenticationCache"/>
    <property name="authorizationCachingEnabled" value="true"/>
    <property name="authorizationCacheName" value="authorizationCache"/>

    <property name="clientId" value="c1ebe466-1cdc-4bd3-ab69-77c3561b9dee"/>
    <property name="clientSecret" value="d8346ea2-6017-43ed-ad68-19c0f971738b"/>
    <property name="accessTokenUrl" value="http://localhost:8080/chapter17-server/accessToken"/>
    <property name="userInfoUrl" value="http://localhost:8080/chapter17-server/userInfo"/>
    <property name="redirectUrl" value="http://localhost:9080/chapter17-client/oauth2-login"/>
</bean>
```
此OAuth2Realm需要配置在服务端申请的clientId和clientSecret；及用于根据auth code换取access token的accessTokenUrl地址；
及用于根据access token换取用户信息（受保护资源）的userInfoUrl地址。
```
<!-- OAuth2身份验证过滤器 -->
<bean id="oAuth2AuthenticationFilter" class="onlive.babylove.www.oauth2.OAuth2AuthenticationFilter">
    <property name="authCodeParam" value="code"/>
    <property name="failureUrl" value="/oauth2Failure.jsp"/>
</bean>
```
此OAuth2AuthenticationFilter用于拦截服务端重定向回来的auth code
```
<!-- Shiro的Web过滤器 -->
<bean id="shiroFilter" class="org.apache.shiro.spring.web.ShiroFilterFactoryBean">
    <property name="securityManager" ref="securityManager"/>
    <property name="loginUrl" value="http://localhost:8080/chapter17-server/authorize?client_id=c1ebe466-1cdc-4bd3-ab69-77c3561b9dee&amp;response_type=code&amp;redirect_uri=http://localhost:9080/chapter17-client/oauth2-login"/>
    <property name="successUrl" value="/"/>
    <property name="filters">
        <util:map>
            <entry key="oauth2Authc" value-ref="oAuth2AuthenticationFilter"/>
        </util:map>
    </property>
    <property name="filterChainDefinitions">
        <value>
            / = anon
            /oauth2Failure.jsp = anon
            /oauth2-login = oauth2Authc
            /logout = logout
            /** = user
        </value>
    </property>
</bean>
```
此处设置loginUrl为http://localhost:8080/chapter17-server/authorize?client_id=c1ebe466-1cdc-4bd3-ab69-77c3561b9dee&response_type=code&redirect_uri=http://localhost:9080/chapter17-client/oauth2-login"；
其会自动设置到所有的AccessControlFilter，如oAuth2AuthenticationFilter；
另外/oauth2-login = oauth2Authc表示/oauth2-login地址使用oauth2Authc拦截器拦截并进行oauth2客户端授权。

###测试
1. 首先访问http://localhost:8070/chapter17-client/，然后点击登录按钮进行登录(端口和项目名可能会根据环境而变化)
2. 输入用户名进行登录并授权；
3. 如果登录成功，服务端会重定向到客户端，即之前客户端提供的地址http://localhost:9080/chapter17-client/oauth2-login?code=473d56015bcf576f2ca03eac1a5bcc11，并带着auth code过去；

