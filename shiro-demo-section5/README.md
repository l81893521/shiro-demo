在涉及到密码存储问题上，应该加密/生成密码摘要存储，而不是存储明文密码。
比如之前的600w csdn账号泄露对用户可能造成很大损失，因此应加密/生成不可逆的摘要方式存储。

###5.1 编码/解码
Shiro提供了base64和16进制字符串编码/解码的API支持，
方便一些编码解码操作。Shiro内部的一些数据的存储/表示都使用了base64和16进制字符串。

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/CodecAndCryptoTest.java)
```
@Test
public void testBase64(){
    String str = "hello";
    String base64Encode = Base64.encodeToString(str.getBytes());
    String str2 = Base64.decodeToString(base64Encode);
    Assert.assertEquals(str, str2);
}
```
通过如上方式可以进行base64编码/解码操作，更多API请参考其Javadoc。
```
@Test
public void testHex(){
    String str = "hello";
    String hexEncode = Hex.encodeToString(str.getBytes());
    String str2 = new String(Hex.decode(hexEncode.getBytes()));
    Assert.assertEquals(str, str2);
}
```
通过如上方式可以进行16进制字符串编码/解码操作，更多API请参考其Javadoc。
```
@Test
public void testCodeSupport(){
    String str = "hello";
    byte[] bytes = CodecSupport.toBytes(str, "utf-8");
    String str2 = CodecSupport.toString(bytes, "utf-8");
    Assert.assertEquals(str, str2);
}
```
还有一个可能经常用到的类CodecSupport，提供了toBytes(str, "utf-8") / toString(bytes, "utf-8")用于在byte数组/String之间转换。

###5.2散列算法
散列算法一般用于生成数据的摘要信息，是一种不可逆的算法，一般适合存储密码之类的数据，常见的散列算法如MD5、SHA等。

一般进行散列时最好提供一个salt（盐），比如加密密码“admin”，产生的散列值是“21232f297a57a5a743894a0e4a801fc3”，
可以到一些md5解密网站很容易的通过散列值得到密码“admin”，即如果直接对密码进行散列相对来说破解更容易，
此时我们可以加一些只有系统知道的干扰数据，如用户名和ID（即盐）；

这样散列的对象是“密码+用户名+ID”，这样生成的散列值相对来说更难破解。

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/CodecAndCryptoTest.java)
```
@Test
public void testMd5(){
    String str = "hello";
    String salt = "123";
    String md5 = new Md5Hash(str, salt).toString();
    System.out.println(md5);
}
```
如上代码通过盐“123”MD5散列“hello”。
另外散列时还可以指定散列次数，如2次表示：md5(md5(str))：“new Md5Hash(str, salt, 2).toString()”。
```
@Test
public void testSha256(){
    String str = "hello";
    String salt = "123";
    String sha256 = new Sha256Hash(str, salt).toString();
    System.out.println(sha256);
}
```
使用SHA256算法生成相应的散列数据，另外还有如SHA1、SHA512算法

Shiro还提供了通用的散列支持：
```
@Test
public void testSimpleHash(){
    String str = "hello";
    String salt = "123";
    String simpleHash = new SimpleHash("SHA-1", str, salt).toString();
    System.out.println(simpleHash);
}
```
通过调用SimpleHash时指定散列算法，其内部使用了Java的MessageDigest实现。

为了方便使用，Shiro提供了HashService，默认提供了DefaultHashService实现。
```
@Test
public void testHashService(){
    DefaultHashService hashService = new DefaultHashService();
    hashService.setHashAlgorithmName("SHA-512");
    //私盐
    hashService.setPrivateSalt(new SimpleByteSource("123"));
    //是否生成公盐
    hashService.setGeneratePublicSalt(true);
    //用于生成公盐,默认就这个
    hashService.setRandomNumberGenerator(new SecureRandomNumberGenerator());
    //生成hash值的迭代次数
    hashService.setHashIterations(1);

    HashRequest request = new HashRequest.Builder().setAlgorithmName("MD5")
            .setSource(ByteSource.Util.bytes("hello"))
            .setSalt(ByteSource.Util.bytes("123"))
            .setIterations(2)
            .build();

    String hex = hashService.computeHash(request).toHex();
    System.out.println(hex);
}
```
1. 首先创建一个DefaultHashService，默认使用SHA-512算法；
2. 可以通过hashAlgorithmName属性修改算法；
3. 可以通过privateSalt设置一个私盐，其在散列时自动与用户传入的公盐混合产生一个新盐；
4. 可以通过generatePublicSalt属性在用户没有传入公盐的情况下是否生成公盐；
5. 可以设置randomNumberGenerator用于生成公盐；
6. 可以设置hashIterations属性来修改默认加密迭代次数；
7. 需要构建一个HashRequest，传入算法、数据、公盐、迭代次数。

SecureRandomNumberGenerator用于生成一个随机数：
```
@Test
public void testRandom(){
    SecureRandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
    randomNumberGenerator.setSeed("123".getBytes());
    System.out.println(randomNumberGenerator.nextBytes().toHex());
}
```

###5.3加密/解密

Shiro还提供对称式加密/解密算法的支持，如AES、Blowfish等；当前还没有提供对非对称加密/解密算法支持，未来版本可能提供。

AES算法实现：[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/CodecAndCryptoTest.java)
```
@Test
public void testAesCipherService(){
    AesCipherService aesCipherService = new AesCipherService();
    //设置key长度
    aesCipherService.setKeySize(128);

    Key key = aesCipherService.generateNewKey();

    String text = "hello";

    //加密
    String encrpText = aesCipherService.encrypt(text.getBytes(), key.getEncoded()).toHex();
    //解密
    String text2 = new String(aesCipherService.decrypt(Hex.decode(encrpText), key.getEncoded()).getBytes());

    Assert.assertEquals(text, text2);
}
```
Blosfish算法实现:
```
@Test
public void testBlowfishCipherService(){
    BlowfishCipherService blowfishCipherService = new BlowfishCipherService();
    blowfishCipherService.setKeySize(128);

    Key key = blowfishCipherService.generateNewKey();

    String text = "hello";

    //加密
    String encrp = blowfishCipherService.encrypt(text.getBytes(), key.getEncoded()).toHex();
    //解密
    String text2 = new String(blowfishCipherService.decrypt(Hex.decode(encrp), key.getEncoded()).getBytes());

    Assert.assertEquals(text, text2);
}
```
更多算法请查看:[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/CodecAndCryptoTest.java)

###5.4 PasswordService/CredentialsMatcher

Shiro提供了PasswordService及CredentialsMatcher用于提供加密密码及验证密码服务。

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/realm/MyRealm.java)
```
public class MyRealm extends AuthorizingRealm{

    private PasswordService passwordService;

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo("zhang", passwordService.encryptPassword("123"), getName());
    }
}
```
为了方便，直接注入一个passwordService来加密密码，实际使用时需要在Service层使用passwordService加密密码并存到数据库。

shiro-passwordService.ini配置 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/resources/shiro-passwordService.ini)
```
passwordService=org.apache.shiro.authc.credential.DefaultPasswordService
hashService=org.apache.shiro.crypto.hash.DefaultHashService
passwordService.hashService=$hashService
hashFormat=org.apache.shiro.crypto.hash.format.Shiro1CryptFormat
passwordService.hashFormat=$hashFormat
hashFormatFactory=org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory
passwordService.hashFormatFactory=$hashFormatFactory

passwordMatcher=org.apache.shiro.authc.credential.PasswordMatcher
passwordMatcher.passwordService=$passwordService

myRealm=realm.MyRealm
myRealm.passwordService=$passwordService
myRealm.credentialsMatcher=$passwordMatcher
securityManager.realms=$myRealm
```

1. passwordService使用DefaultPasswordService，如果有必要也可以自定义；
2. hashService定义散列密码使用的HashService，默认使用DefaultHashService（默认SHA-512算法）；
3. hashFormat用于对散列出的值进行格式化，默认使用Shiro1CryptFormat，另外提供了Base64Format和HexFormat，
对于有salt的密码请自定义实现ParsableHashFormat然后把salt格式化到散列值中；
4. hashFormatFactory用于根据散列值得到散列的密码和salt；因为如果使用如SHA算法，那么会生成一个salt，
此salt需要保存到散列后的值中以便之后与传入的密码比较时使用；默认使用DefaultHashFormatFactory；
5. passwordMatcher使用PasswordMatcher，其是一个CredentialsMatcher实现；
6. 将credentialsMatcher赋值给myRealm，myRealm间接继承了AuthenticatingRealm，其在调用getAuthenticationInfo方法获取到AuthenticationInfo信息后，
会使用credentialsMatcher来验证凭据是否匹配，如果不匹配将抛出IncorrectCredentialsException异常。

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
@Test
public void testPasswordServiceWithMyrealm(){
    login("classpath:shiro-passwordService.ini","zhang","123");
}
```

使用JdbcRealm实现:

初始化sql数据 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/sql/shiro-init-data.sql)
```
insert into users(username, password, password_salt) values('wu', '$shiro1$SHA-512$1$$PJkJr+wlNU1VHa4hWQuybjjVPyFzuNPcPu5MBH56scHri4UQPjvnumE7MbtcnDYhTcnxSkL9ei/bhIVrylxEwg==', null);
insert into users(username, password, password_salt) values('liu', 'a9a114054aa6758184314fbb959fbda4', '24520ee264eab73ec09451d0e9ea6aac');
```

shiro-jdbc-passwordService.ini [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/resources/shiro-jdbc-passwordService.ini)
```
passwordService=org.apache.shiro.authc.credential.DefaultPasswordService
hashService=org.apache.shiro.crypto.hash.DefaultHashService
passwordService.hashService=$hashService
hashFormat=org.apache.shiro.crypto.hash.format.Shiro1CryptFormat
passwordService.hashFormat=$hashFormat
hashFormatFactory=org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory
passwordService.hashFormatFactory=$hashFormatFactory

passwordMatcher=org.apache.shiro.authc.credential.PasswordMatcher
passwordMatcher.passwordService=$passwordService

dataSource=com.alibaba.druid.pool.DruidDataSource
dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql://192.168.31.188:3306/shiro
dataSource.username=root
dataSource.password=YEEkoo@2016

jdbcRealm=org.apache.shiro.realm.jdbc.JdbcRealm
jdbcRealm.dataSource=$dataSource
jdbcRealm.permissionsLookupEnabled=true
jdbcRealm.credentialsMatcher=$passwordMatcher
securityManager.realms=$jdbcRealm
```

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
@Test
public void testPasswordServiceWithJdbcRealm(){
    login("classpath:shiro-jdbc-passwordService.ini","zhang","123");
}
```

如上方式的缺点是：salt保存在散列值中；没有实现如密码重试次数限制。

**HashedCredentialsMatcher实现密码验证服务**

Shiro提供了CredentialsMatcher的散列实现HashedCredentialsMatcher，和之前的PasswordMatcher不同的是，它只用于密码验证，
且可以提供自己的盐，而不是随机生成盐，且生成密码散列值的算法需要自己写，因为能提供自己的盐。

**生成密码散列值**

此处我们使用MD5算法，“密码+盐（用户名+随机数）”的方式生成散列值：

 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
String algorithmName = "md5";
String username = "liu";
String password = "123";
String salt1 = username;
String salt2 = new SecureRandomNumberGenerator().nextBytes().toHex();
int hashIterations = 2;

SimpleHash hash = new SimpleHash(algorithmName, password, salt1 + salt2, hashIterations);

String encodedPassword = hash.toHex();
```
如果要写用户模块，需要在新增用户/重置密码时使用如上算法保存密码，
将生成的密码及salt2存入数据库（因为我们的散列算法是：md5(md5(密码+username+salt2))）。

shiro-hashedCredentialsMatcher.ini配置 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/resources/shiro-hashedCredentialsMatcher.ini)
```
credentialsMatcher=org.apache.shiro.authc.credential.HashedCredentialsMatcher
credentialsMatcher.hashAlgorithmName=md5
credentialsMatcher.hashIterations=2
credentialsMatcher.storedCredentialsHexEncoded=true

myRealm=realm.MyRealm2
myRealm.credentialsMatcher=$credentialsMatcher
securityManager.realms=$myRealm
```

1. 通过credentialsMatcher.hashAlgorithmName=md5指定散列算法为md5，需要和生成密码时的一样；
2. credentialsMatcher.hashIterations=2，散列迭代次数，需要和生成密码时的一样
3. credentialsMatcher.storedCredentialsHexEncoded=true表示是否存储散列后的密码为16进制，需要和生成密码时的一样，默认是base64

此处最需要注意的就是HashedCredentialsMatcher的算法需要和生成密码时的算法一样。

另外HashedCredentialsMatcher会自动根据AuthenticationInfo的类型是否是SaltedAuthenticationInfo来获取credentialsSalt盐。


**生成Realm**

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/realm/MyRealm2.java)
```
public class MyRealm2 extends AuthorizingRealm{
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = "liu";
        //随机数
        String salt2 = "0072273a5d87322163795118fdd7c45e";
        //加密后的密码
        String password = "be320beca57748ab9632c4121ccac0db";

        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(username, password, getName());
        authenticationInfo.setCredentialsSalt(ByteSource.Util.bytes(username + salt2));
        return authenticationInfo;
    }
}
```

此处就是把步骤1中生成的相应数据组装为SimpleAuthenticationInfo
通过SimpleAuthenticationInfo的credentialsSalt设置盐，HashedCredentialsMatcher会自动识别这个盐。

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
@Test
public void testHashedCredentialsMatcherWithMyRealm2(){
    //使用testGeneratePassword生成的散列密码
    login("classpath:shiro-hashedCredentialsMatcher.ini", "liu", "123");
}
```

**使用JdbcRealm来验证**

shiro-jdbc-hashedCredentialsMatcher.ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/resources/shiro-jdbc-hashedCredentialsMatcher.ini)
```
credentialsMatcher=org.apache.shiro.authc.credential.HashedCredentialsMatcher
credentialsMatcher.hashAlgorithmName=md5
credentialsMatcher.hashIterations=2
credentialsMatcher.storedCredentialsHexEncoded=true

dataSource=com.alibaba.druid.pool.DruidDataSource
dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql://192.168.31.188:3306/shiro
dataSource.username=root
dataSource.password=YEEkoo@2016

jdbcRealm=org.apache.shiro.realm.jdbc.JdbcRealm
jdbcRealm.dataSource=$dataSource
jdbcRealm.permissionsLookupEnabled=true
jdbcRealm.saltStyle=COLUMN
jdbcRealm.authenticationQuery=select password, concat(username,password_salt) from users where username = ?
jdbcRealm.credentialsMatcher=$credentialsMatcher
securityManager.realms=$jdbcRealm
```
修改获取用户信息（包括盐）的sql：“select password, password_salt from users where username = ?”，
而我们的盐是由username+password_salt组成，所以需要通过ini配置修改

1. saltStyle表示使用密码+盐的机制，authenticationQuery第一列是密码，第二列是盐；
2. 通过authenticationQuery指定密码及盐查询SQL；

此处还要注意Shiro默认使用了apache commons BeanUtils，默认是不进行Enum类型转型的，此时需要自己注册一个Enum转换器

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
private class EnumConverter extends AbstractConverter {

    protected String convertToString(final Object value) throws Throwable {
        return ((Enum)value).name();
    }

    protected Object convertToType(Class type, Object value) throws Throwable {
        return Enum.valueOf(type, value.toString());
    }

    protected Class getDefaultType() {
        return null;
    }
}
```

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
@Test
public void testHashedCredentialsMatcherWithJdbcRealm(){

    BeanUtilsBean.getInstance().getConvertUtils().register(new EnumConverter(), JdbcRealm.SaltStyle.class);
    //使用testGeneratePassword生成的散列密码
    login("classpath:shiro-jdbc-hashedCredentialsMatcher.ini", "liu", "123");
}
```

测试前请先调用sql/shiro-init-data.sql初始化用户数据。

**密码重试次数限制**

如在1个小时内密码最多重试5次，如果尝试次数超过5次就锁定1小时，1小时后可再次重试，如果还是重试失败，可以锁定如1天，以此类推，防止密码被暴力破解。

我们通过继承HashedCredentialsMatcher，且使用Ehcache记录重试次数和超时时间。

[查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/credentials/RetryLimitHashedCredentialsMatcher.java)
```
public class RetryLimitHashedCredentialsMatcher extends HashedCredentialsMatcher{

    private Ehcache passwordRetryCache;

    public RetryLimitHashedCredentialsMatcher() {
        CacheManager cacheManager = CacheManager.newInstance(CacheManager.class.getClassLoader().getResource("ehcache.xml"));
        passwordRetryCache = cacheManager.getCache("passwordRetryCache");
    }

    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info){
        String username = (String) token.getPrincipal();

        Element element = passwordRetryCache.get(username);
        if(element == null){
            element = new Element(username, new AtomicInteger(0));
            passwordRetryCache.put(element);
        }

        AtomicInteger retryCount = (AtomicInteger) element.getObjectValue();

        if(retryCount.incrementAndGet() > 5){
            throw new ExcessiveAttemptsException();
        }

        boolean matches = super.doCredentialsMatch(token, info);
        if(matches){
            passwordRetryCache.remove(username);
        }
        return matches;
    }
}
```

ini配置文件 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/resources/shiro-retryLimitHashedCredentialsMatcher.ini)
```
credentialsMatcher=org.apache.shiro.authc.credential.HashedCredentialsMatcher
credentialsMatcher.hashAlgorithmName=md5
credentialsMatcher.hashIterations=2
credentialsMatcher.storedCredentialsHexEncoded=true

myRealm=realm.MyRealm2
myRealm.credentialsMatcher=$credentialsMatcher
securityManager.realms=$myRealm
```

测试用例 [查看代码](https://github.com/l81893521/shiro-demo/blob/master/shiro-demo-section5/src/test/java/PasswordTest.java)
```
@Test(expected = ExcessiveAttemptsException.class)
public void testRetryLimitHashedCredentialsMatcherWithMyRealm(){
    for (int i = 1; i <= 5; i++) {
        try {
            login("classpath:shiro-retryLimitHashedCredentialsMatcher.ini", "liu", "1234");
        } catch (Exception e){
            //前五次会抛出IncorrectCredentialsException,忽略掉
        }
    }
    login("classpath:shiro-retryLimitHashedCredentialsMatcher.ini", "liu", "1234");
}
```