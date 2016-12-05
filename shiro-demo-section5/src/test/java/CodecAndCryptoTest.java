import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.Hex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Will.Zhang on 2016/12/2 0002 18:24.
 */
public class CodecAndCryptoTest {

    @Test
    public void testBase64(){
        String str = "hello";
        String base64Encode = Base64.encodeToString(str.getBytes());
        String str2 = Base64.decodeToString(base64Encode);
        Assert.assertEquals(str, str2);
    }

    @Test
    public void testHex(){
        String str = "hello";
        String hexEncode = Hex.encodeToString(str.getBytes());
        String str2 = new String(Hex.decode(hexEncode.getBytes()));
        Assert.assertEquals(str, str2);
    }
}
