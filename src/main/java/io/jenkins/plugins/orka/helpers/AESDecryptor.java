
package io.jenkins.plugins.orka.helpers;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryptor {
    private static final Logger logger = Logger.getLogger(AESDecryptor.class.getName());

    private static final int INDEX_KEY = 0;
    private static final int INDEX_IV = 1;
    private static final int ITERATIONS = 1;

    private static final int SALT_OFFSET = 8;
    private static final int SALT_SIZE = 8;
    private static final int CIPHERTEXT_OFFSET = SALT_OFFSET + SALT_SIZE;

    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String DIGEST = "MD5";
    private static final String AES = "AES";

    private static final int KEY_SIZE_BITS = 256;

    public static String decrypt(String cypherText, String password) {
        try {
            byte[] dataBase64 = Base64.getDecoder().decode(cypherText);
            byte[] salt = Arrays.copyOfRange(dataBase64, SALT_OFFSET, SALT_OFFSET + SALT_SIZE);
            byte[] encrypted = Arrays.copyOfRange(dataBase64, CIPHERTEXT_OFFSET, dataBase64.length);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            MessageDigest sha1 = MessageDigest.getInstance(DIGEST);

            final byte[][] keyAndIV = EVP_BytesToKey(KEY_SIZE_BITS / Byte.SIZE, cipher.getBlockSize(), sha1, salt,
                    password.getBytes("ASCII"), ITERATIONS);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[INDEX_KEY], AES);
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[INDEX_IV]);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] decrypted = cipher.doFinal(encrypted);
            String answer = new String(decrypted, "UTF-8");
            return answer;

        } catch (Exception ex) {
            logger.log(Level.WARNING, "Decryption failed", ex);
        }
        return null;
    }

    // DISABLE CHECKSTYLE

    private static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt, byte[] data,
            int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (;;) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }

    // ENABLE CHECKSTYLE
}