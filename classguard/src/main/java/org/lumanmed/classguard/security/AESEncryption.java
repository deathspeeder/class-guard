package org.lumanmed.classguard.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Willard Wang
 * 
 */
public class AESEncryption implements IEncryption {
    private SecretKey keySpec;
    private IvParameterSpec ivSpec;
    private Cipher cipher;

    public AESEncryption() {

    }

    public void init(String... key) {
        if (key.length == 0) {
            throw new IllegalArgumentException("At least one key is needed.");
        } else if (key.length == 1) {
            init(key[0], key[0]);
        } else {
            init(key[0], key[1]);
        }
    }

    public AESEncryption(String secretKey, String iv) {
        init(secretKey, iv);
    }

    protected void init(String secretKey, String iv) {
        keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        ivSpec = new IvParameterSpec(iv.getBytes());
        try {
            cipher = Cipher.getInstance("AES/CFB/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(e);
        } catch (NoSuchPaddingException e) {
            throw new SecurityException(e);
        }
    }

    public byte[] encrypt(byte[] data) {
        try {
            if (cipher == null) {
                throw new IllegalStateException("Call init(String...) first.");
            }
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException(e);
        } catch (BadPaddingException e) {
            throw new SecurityException(e);
        } catch (InvalidKeyException e) {
            throw new SecurityException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }
    }

    public byte[] decrypt(byte[] encryptedData) {
        try {
            if (cipher == null) {
                throw new IllegalStateException("Call init(String...) first.");
            }
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(encryptedData);
        } catch (IllegalBlockSizeException e) {
            throw new SecurityException(e);
        } catch (BadPaddingException e) {
            throw new SecurityException(e);
        } catch (InvalidKeyException e) {
            throw new SecurityException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new SecurityException(e);
        }
    }
}
