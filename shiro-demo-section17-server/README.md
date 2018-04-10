目前很多开放平台如新浪微博开放平台都在使用提供````开放API接口供开发者使用，
随之带来了第三方应用要到开放平台进行授权的问题，OAuth就是干这个的，OAuth2是OAuth协议的下一个版本，相比OAuth1，
OAuth2整个授权流程更简单安全了，但不兼容OAuth1，具体可以到OAuth2官网http://oauth.net/2/查看，
OAuth2协议规范可以参考http://tools.ietf.org/html/rfc6749。目前有好多参考实现供选择，可以到其官网查看下载。

本文使用Apache Oltu，其之前的名字叫Apache Amber ，是Java版的参考实现。
使用文档可参考https://cwiki.apache.org/confluence/display/OLTU/Documentation。

###OAuth角色
**资源拥有者（resource owner）：**能授权访问受保护资源的一个实体，可以是一个人，那我们称之为最终用户；如新浪微博用户zhangsan；

**资源服务器（resource server）：**存储受保护资源，客户端通过access token请求资源，资源服务器响应受保护资源给客户端；存储着用户zhangsan的微博等信息。

**授权服务器（authorization server）：**成功验证资源拥有者并获取授权之后，授权服务器颁发授权令牌（Access Token）给客户端。

**客户端（client）：**如新浪微博客户端weico、微格等第三方应用，也可以是它自己的官方应用；其本身不存储资源，而是资源拥有者授权通过后，
使用它的授权（授权令牌）访问受保护资源，然后客户端把相应的数据展示出来/提交到服务器。
“客户端”术语不代表任何特定实现（如应用运行在一台服务器、桌面、手机或其他设备）。

![](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/image/1.png)

1. 客户端从资源拥有者那请求授权。授权请求可以直接发给资源拥有者，或间接的通过授权服务器这种中介，后者更可取。
2. 客户端收到一个授权许可，代表资源服务器提供的授权。
3. 客户端使用它自己的私有证书及授权许可到授权服务器验证。
4. 如果验证成功，则下发一个访问令牌。
5. 客户端使用访问令牌向资源服务器请求受保护资源。
6. 资源服务器会验证访问令牌的有效性，如果成功则下发受保护资源。

更多流程的解释请参考OAuth2的协议规范http://tools.ietf.org/html/rfc6749。

###服务器端

本文把授权服务器和资源服务器整合在一起实现。

###POM依赖
此处我们使用apache oltu oauth2服务端实现，需要引入authzserver（授权服务器依赖）和resourceserver（资源服务器依赖）。

```
<!-- authzserver授权服务器 -->
<dependency>
    <groupId>org.apache.oltu.oauth2</groupId>
    <artifactId>org.apache.oltu.oauth2.authzserver</artifactId>
    <version>1.0.2</version>
</dependency>
<!-- resourceserver资源服务器 -->
<dependency>
    <groupId>org.apache.oltu.oauth2</groupId>
    <artifactId>org.apache.oltu.oauth2.resourceserver</artifactId>
    <version>1.0.2</version>
</dependency>
```
其他的请参考pom.xml。

###数据字典
用户(oauth2_user)

| 名称        | 类型           | 长度  | 描述 |
| ------------- |:-------------:| -----:| ---- |
| id      | bigint | 10 | 编号 主键 |
| username     | varchar      |   100 | 用户名 |
| password | varchar      |    100 | 密码 |
| salt | varchar      |    50 | 盐 |

客户端(oauth2_client)

| 名称        | 类型           | 长度  | 描述 |
| ------------- |:-------------:| -----:| ---- |
| id      | bigint | 10 | 编号 主键 |
| client_name     | varchar      |   100 | 客户端名称 |
| client_id | varchar      |    100 | 客户端id |
| client_secret | varchar      |    100 | 客户端安全key |

用户表存储着认证/资源服务器的用户信息，即资源拥有者；比如用户名/密码；

客户端表存储客户端的的客户端id及客户端安全key；在进行授权时使用。

###表及数据SQL
具体请参考

[查看代码-表结构](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/shiro-schema.sql)

[查看代码-初始数据](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/shiro-data.sql)

默认用户名/密码是admin/123456。

###实体
[查看代码-实体](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/main/entity)

###DAO
[查看代码-dao](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/main/dao)

###Service
[查看代码-service](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/main/service)

关键接口OAuthServiceImpl
```
public interface OAuthService {
   public void addAuthCode(String authCode, String username);// 添加 auth code
   public void addAccessToken(String accessToken, String username); // 添加 access token
   boolean checkAuthCode(String authCode); // 验证auth code是否有效
   boolean checkAccessToken(String accessToken); // 验证access token是否有效
   String getUsernameByAuthCode(String authCode);// 根据auth code获取用户名
   String getUsernameByAccessToken(String accessToken);// 根据access token获取用户名
   long getExpireIn();//auth code / access token 过期时间
   public boolean checkClientId(String clientId);// 检查客户端id是否存在
   public boolean checkClientSecret(String clientSecret);// 坚持客户端安全KEY是否存在
}
```
此处通过OAuthService实现进行auth code和access token的维护。

###后端数据维护控制器

[查看代码-controller](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17/sql/main/controller)

IndexController、LoginController、UserController和ClientController，其用于维护后端的数据，如用户及客户端数据；即相当于后台管理。

###授权控制器AuthorizeController
```
/**
 * Created by Will.Zhang on 2017/4/17 0017 17:51.
 * 授权控制器
 */
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
                    return "oauth2login";
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
            headers.setLocation(new URI(redirectURI));
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
```
如上代码的作用：
1. 首先通过如http://localhost:8080/chapter17-server/authorize?client_id=c1ebe466-1cdc-4bd3-ab69-77c3561b9dee&response_type=code&redirect_uri=http://localhost:9080/chapter17-client/oauth2-login访问授权页面
2. 该控制器首先检查clientId是否正确；如果错误将返回相应的错误信息
3. 然后判断用户是否登录了，如果没有登录首先到登录页面登录
4. 登录成功后生成相应的auth code即授权码，然后重定向到客户端地址，如http://localhost:9080/chapter17-client/oauth2-login?code=52b1832f5dff68122f4f00ae995da0ed；
在重定向到的地址中会带上code参数（授权码），接着客户端可以根据授权码去换取access token。

###问令牌控制器AccessTokenController
```
@RestController
public class AccessTokenController {

    @Autowired
    private OAuthService oAuthService;

    @Autowired
    private UserService userService;

    @RequestMapping("/accessToken")
    public HttpEntity token(HttpServletRequest request) throws OAuthSystemException {
        try {
            //构建oauth请求
            OAuthTokenRequest oAuthTokenRequest = new OAuthTokenRequest(request);
            //检查client_id是否正确
            if (!oAuthService.checkClientId(oAuthTokenRequest.getClientId())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_DESCRIPTION)
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }

            // 检查客户端安全KEY是否正确
            if (!oAuthService.checkClientSecret(oAuthTokenRequest.getClientSecret())) {
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                                .setErrorDescription(Constants.INVALID_CLIENT_DESCRIPTION)
                                .buildJSONMessage();
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }

            String authCode = oAuthTokenRequest.getParam(OAuth.OAUTH_CODE);
            //检查验证类型, 此处只检查AUTHORIZATION_CODE类型, 其他的还有PASSWORD或REFRESH_TOKEN
            if(oAuthTokenRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())){
                if(!oAuthService.checkAuthCode(authCode)){
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("错误的授权码")
                            .buildJSONMessage();
                    return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
                }
            }

            //生成AccessToken
            OAuthIssuer oAuthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
            final String accessToken = oAuthIssuerImpl.accessToken();
            oAuthService.addAccessToken(accessToken, oAuthService.getUsernameByAuthCode(authCode));


            //生成OAuth响应
            OAuthResponse response = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setExpiresIn(String.valueOf(oAuthService.getExpireIn()))
                    .buildJSONMessage();

            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));

        } catch (OAuthProblemException e) {
            //构建错误响应
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return new ResponseEntity(res.getBody(), HttpStatus.valueOf(res.getResponseStatus()));
        }
    }
}
```
如上代码的作用:
1. http://localhost:8080/chapter17-server/accessToken，POST提交如下数据：client_id= c1ebe466-1cdc-4bd3-ab69-77c3561b9dee& client_secret= d8346ea2-6017-43ed-ad68-19c0f971738b&grant_type=authorization_code&code=828beda907066d058584f37bcfd597b6&redirect_uri=http://localhost:9080/chapter17-client/oauth2-login访问
2. 该控制器会验证client_id、client_secret、auth code的正确性，如果错误会返回相应的错误
3. 如果验证通过会生成并返回相应的访问令牌access token

###资源控制器UserInfoController
```
@RestController
public class UserInfoController {

    @Autowired
    private OAuthService oAuthService;

    @RequestMapping("/userInfo")
    public HttpEntity userInfo(HttpServletRequest request) throws OAuthSystemException {
        try {
            //构建OAuth资源请求
            OAuthAccessResourceRequest resourceRequest = new OAuthAccessResourceRequest(request);
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
```

如上代码的作用：
1. 首先通过如http://localhost:8080/chapter17-server/userInfo? access_token=828beda907066d058584f37bcfd597b6进行访问；
2. 该控制器会验证access token的有效性；如果无效了将返回相应的错误，客户端再重新进行授权；
3. 如果有效，则返回当前登录用户的用户名。

###Spring配置文件
此处只列举spring-config-shiro.xml中的shiroFilter的filterChainDefinitions属性：
```
<property name="filterChainDefinitions">
            <value>
                / = anon
                /login = authc
                /logout = logout

                /authorize=anon
                /accessToken=anon
                /userInfo=anon

                /** = user
            </value>
        </property>
```
对于oauth2的几个地址/authorize、/accessToken、/userInfo都是匿名可访问的。

###服务器维护
访问localhost:8080/chapter17-server/，登录后进行客户端管理和用户管理。
客户端管理就是进行客户端的注册，如新浪微博的第三方应用就需要到新浪微博开发平台进行注册；用户管理就是进行如新浪微博用户的管理。

对于授权服务和资源服务的实现可以参考新浪微博开发平台的实现：

http://open.weibo.com/wiki/授权机制说明

http://open.weibo.com/wiki/微博API

###客户端
[查看客户端](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section17-client)