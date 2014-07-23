/**
 * 
 */
package org.lumanmed.classguard.security;

/**
 * @author Willard Wang
 *
 */
public interface IEncryption {
    void init(String... key);
    byte[] encrypt(byte[] data);
    byte[] decrypt(byte[] data);
}
