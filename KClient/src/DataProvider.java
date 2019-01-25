import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Kevin Scholten on Jan, 2019
 */


public class DataProvider {
    static DataProvider instance = new DataProvider();
    PrivateKey PRIVATE_KEY;
    PublicKey PUBLIC_KEY;

    public static DataProvider getInstance() {
        return instance;
    }

//    public byte[] encrypt(PublicKey key, byte[] plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
//    {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, key);
//        return cipher.doFinal(plaintext);
//    }
//
//    public byte[] decrypt(PrivateKey key, byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
//    {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, key);
//        return cipher.doFinal(ciphertext);
//    }
}
