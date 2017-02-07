package credentials;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Will.Zhang on 2017/2/7 0007 16:09.
 */
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher{

    private Ehcache passwordRetryCache;

    public RetryLimitHashedCredentialsMatcher() {
        CacheManager cacheManager = CacheManager.newInstance(CacheManager.class.getClassLoader().getResource("ehcache.xml"));
        passwordRetryCache = cacheManager.getCache("passwordRetryCache");
    }

    /**
     * 增加重试次数限制,验证方法不改变
     * @param token
     * @param info
     * @return
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {

        String username = (String) token.getPrincipal();

        //retry count + 1
        Element element = passwordRetryCache.get(username);
        if(element == null){
            element = new Element(username, new AtomicInteger(0));
            passwordRetryCache.put(element);
        }

        AtomicInteger retryCount = (AtomicInteger) element.getObjectValue();

        if(retryCount.incrementAndGet() > 5){
            throw new ExcessiveAttemptsException();
        }

        //验证还是调用父类的,不改变
        boolean matches = super.doCredentialsMatch(token, info);
        if(matches){
            // clear retry count
            passwordRetryCache.remove(username);
        }
        return matches;
    }
}
