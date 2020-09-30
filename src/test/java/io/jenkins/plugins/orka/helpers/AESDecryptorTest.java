package io.jenkins.plugins.orka.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class AESDecryptorTest {

    /*
     * Encrypted values are generated using openssl. Command used: echo value |
     * openssl enc -a -aes-256-cbc -pass pass:pass This ensures compatibility of the
     * decryption algorithm with openssl
     */

    private String long_key = "hvqbqpaqdtmlpoxmcupzneahjwpfyxtf";
    private String short_key = "key";
    private String clearText = "extremely_random_text_here";

    @Test
    public void when_decrypting_with_correct_key_should_decrypt_successfully() {
        String encryptedText = "U2FsdGVkX18xeT4ilcTy8zG7McxpVqlW5CYpaBPEc0NuIv3cyk/uu13rNJMr9Hq4";

        String decryptedText = AESDecryptor.decrypt(encryptedText, long_key);
        assertEquals(clearText, decryptedText.trim());
    }

    @Test
    public void when_decrypting_with_correct_short_key_should_decrypt_successfully() {
        String encryptedText = "U2FsdGVkX181Dp2hDTpwuU+aIgdMHVI2T8n+OmZbyPkYsfj3iIl3Qdxtxn55G9P9";

        String decryptedText = AESDecryptor.decrypt(encryptedText, short_key);
        assertEquals(clearText, decryptedText.trim());
    }

    @Test
    public void when_decrypting_with_incorrect_key_should_decrypt_successfully() {
        String encryptedText = "U2FsdGVkX18+kKML2+05tuu1SW6V242h703lbX0926XWChBAO7/MYy2MOpyGCpsZ";

        String decryptedText = AESDecryptor.decrypt(encryptedText, "short_key");
        assertEquals(null, decryptedText);
        assertNotEquals(clearText, decryptedText);
    }
}
