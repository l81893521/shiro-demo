package online.babylove.www.service;

/**
 * Created by Will.Zhang on 2017/4/17 0017 16:04.
 */
public interface OAuthService {

    /**
     * 添加authCode
     * @param authCode
     * @param username
     */
    void addAuthCode(String authCode, String username);

    /**
     * 添加access token
     * @param accessToken
     * @param username
     */
    void addAccessToken(String accessToken, String username);

    /**
     * 验证auth code是否有效
     * @param authCode
     * @return
     */
    boolean checkAuthCode(String authCode);

    /**
     * 验证access token是否有效
     * @param accessToken
     * @return
     */
    boolean checkAccessToken(String accessToken);

    /**
     * 根据authCode获取用户名
     * @param authCode
     * @return
     */
    String getUsernameByAuthCode(String authCode);

    /**
     * 根据accessToken获取用户名
     * @param accessToken
     * @return
     */
    String getUsernameByAccessToken(String accessToken);

    /**
     * auth code / access token 过期时间
     * @return
     */
    long getExpireIn();

    /**
     * 检查clientId是否存在
     * @param clientId
     * @return
     */
    boolean checkClientId(String clientId);

    /**
     * 检查client secret是否存在
     * @param clientSecret
     * @return
     */
    boolean checkClientSecret(String clientSecret);
}
