在涉及到密码存储问题上，应该加密/生成密码摘要存储，而不是存储明文密码。
比如之前的600w csdn账号泄露对用户可能造成很大损失，因此应加密/生成不可逆的摘要方式存储。

###5.1 编码/解码
Shiro提供了base64和16进制字符串编码/解码的API支持，
方便一些编码解码操作。Shiro内部的一些数据的存储/表示都使用了base64和16进制字符串。


```
@Test
public void testBase64(){
    String str = "hello";
    String base64Encode = Base64.encodeToString(str.getBytes());
    String str2 = Base64.decodeToString(base64Encode);
    Assert.assertEquals(str, str2);
}
```
