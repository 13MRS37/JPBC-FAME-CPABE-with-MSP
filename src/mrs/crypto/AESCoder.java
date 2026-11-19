package mrs.crypto;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Symmetric encryption helper used by FAME to encrypt the actual plaintext.
 *
 * <p>The FAME construction produces a secret element in GT. Its byte encoding
 * is used as input keying material (IKM) for HKDF-SHA-256, which derives a
 * 128-bit AES key. The plaintext is then encrypted with AES-GCM.</p>
 *
 * <p>Envelope format (all fields concatenated in this order):</p>
 * <pre>
 * +---------+----------------+--------------------------+
 * | version | 96-bit nonce   | ciphertext || 128-bit tag |
 * | 1 byte  | 12 bytes       | variable length          |
 * +---------+----------------+--------------------------+
 * </pre>
 *
 * <p>The nonce and authentication tag are not secret. The nonce is generated
 * randomly for each encryption. The version byte allows the format to be
 * distinguished from future formats.</p>
 *
 * <p>For applications that bind metadata to the ciphertext, use the overloads
 * that accept Additional Authenticated Data (AAD). AAD is authenticated but
 * is not encrypted. FAME integration can, for example, supply the exact policy
 * string encoded as UTF-8.</p>
 *
 * <p>This class uses only standard JCA/JCE APIs available in modern Java
 * runtimes; it does not require an additional cryptographic library.</p>
 */
public final class AESCoder {

    /** Version identifier for this envelope format. */
    private static final byte FORMAT_VERSION = 0x01;

    /** GCM standard nonce size: 96 bits = 12 bytes. */
    private static final int NONCE_LENGTH_BYTES = 12;

    /** GCM authentication tag size: 128 bits. */
    private static final int TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = TAG_LENGTH_BITS / 8;

    /** AES-128 key size: 128 bits = 16 bytes. */
    private static final int AES_KEY_LENGTH_BYTES = 16;

    /** HMAC-SHA-256 output length: 32 bytes. */
    private static final int HKDF_HASH_LENGTH_BYTES = 32;

    /*
     * HKDF-SHA-256 parameters.
     *
     * The salt is public and fixed; it is not a password and does not need to
     * be secret. The info value domain-separates this key from keys derived
     * for any other purpose. Keep these values stable for this format version.
     */
    private static final byte[] HKDF_SALT =
            "FAME-THESIS-HKDF-SHA256-v1".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] HKDF_INFO =
            "FAME-THESIS-AES-128-GCM-v1".getBytes(StandardCharsets.US_ASCII);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AESCoder() {
        // Utility class: instances are not required.
    }

    /**
     * Encrypts plaintext using a key derived from the supplied FAME secret
     * material. No AAD is supplied by this convenience overload.
     *
     * @param keyMaterial bytes of the recovered/random GT element
     * @param plaintext plaintext bytes (may be empty)
     * @return versioned envelope containing version, nonce, ciphertext and tag
     * @throws GeneralSecurityException if key derivation or encryption fails
     * @throws IllegalArgumentException if an argument is null
     */
    public static byte[] encrypt(byte[] keyMaterial, byte[] plaintext)
            throws GeneralSecurityException {
        return encrypt(keyMaterial, plaintext, new byte[0]);
    }

    /**
     * Encrypts plaintext with AES-128-GCM and authenticates optional metadata.
     *
     * @param keyMaterial input keying material (e.g. msg.toBytes() from FAME)
     * @param plaintext plaintext bytes (may be empty)
     * @param aad additional authenticated data; null is not accepted
     * @return versioned envelope: version || nonce || ciphertext || tag
     * @throws GeneralSecurityException if HKDF or AES-GCM fails
     * @throws IllegalArgumentException if an argument is null
     */
    public static byte[] encrypt(byte[] keyMaterial, byte[] plaintext, byte[] aad)
            throws GeneralSecurityException {
        requireNonNull(keyMaterial, "keyMaterial");
        requireNonNull(plaintext, "plaintext");
        requireNonNull(aad, "aad");

        byte[] aesKey = hkdfSha256(
                keyMaterial, HKDF_SALT, HKDF_INFO, AES_KEY_LENGTH_BYTES);
        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad);

            byte[] ciphertextAndTag = cipher.doFinal(plaintext);

            byte[] envelope = new byte[
                    1 + NONCE_LENGTH_BYTES + ciphertextAndTag.length];
            envelope[0] = FORMAT_VERSION;
            System.arraycopy(nonce, 0, envelope, 1, NONCE_LENGTH_BYTES);
            System.arraycopy(ciphertextAndTag, 0, envelope,
                    1 + NONCE_LENGTH_BYTES, ciphertextAndTag.length);
            return envelope;
        } finally {
            // Best-effort cleanup of the temporary derived key.
            Arrays.fill(aesKey, (byte) 0);
        }
    }

    /**
     * Decrypts an envelope created by {@link #encrypt(byte[], byte[])}.
     * No AAD is supplied by this convenience overload.
     *
     * @param keyMaterial the same input keying material used for encryption
     * @param envelope versioned envelope returned by encrypt
     * @return recovered plaintext
     * @throws GeneralSecurityException if the format is invalid, authentication
     *                                  fails, or decryption otherwise fails
     * @throws IllegalArgumentException if an argument is null
     */
    public static byte[] decrypt(byte[] keyMaterial, byte[] envelope)
            throws GeneralSecurityException {
        return decrypt(keyMaterial, envelope, new byte[0]);
    }

    /**
     * Decrypts and authenticates an AES-GCM envelope, including optional AAD.
     *
     * <p>Any modification to the ciphertext, nonce, tag, or supplied AAD causes
     * authentication to fail (except with negligible cryptographic probability).
     * The caller must handle {@link javax.crypto.AEADBadTagException} or its
     * superclass rather than using unauthenticated plaintext.</p>
     *
     * @param keyMaterial the same input keying material used for encryption
     * @param envelope versioned envelope: version || nonce || ciphertext || tag
     * @param aad the same AAD bytes supplied during encryption
     * @return authenticated plaintext
     * @throws GeneralSecurityException if the format is invalid or authentication
     *                                  fails
     * @throws IllegalArgumentException if an argument is null
     */
    public static byte[] decrypt(byte[] keyMaterial, byte[] envelope, byte[] aad)
            throws GeneralSecurityException {
        requireNonNull(keyMaterial, "keyMaterial");
        requireNonNull(envelope, "envelope");
        requireNonNull(aad, "aad");

        // One version byte + 12-byte nonce + at least a 16-byte GCM tag.
        if (envelope.length < 1 + NONCE_LENGTH_BYTES + TAG_LENGTH_BYTES) {
            throw new GeneralSecurityException("Invalid AES-GCM envelope: too short.");
        }
        if (envelope[0] != FORMAT_VERSION) {
            throw new GeneralSecurityException(
                    "Unsupported AES-GCM envelope version: " + (envelope[0] & 0xFF));
        }

        byte[] nonce = Arrays.copyOfRange(envelope, 1, 1 + NONCE_LENGTH_BYTES);
        byte[] ciphertextAndTag = Arrays.copyOfRange(
                envelope, 1 + NONCE_LENGTH_BYTES, envelope.length);
        byte[] aesKey = hkdfSha256(
                keyMaterial, HKDF_SALT, HKDF_INFO, AES_KEY_LENGTH_BYTES);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BITS, nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad);

            // GCM verifies the tag before returning plaintext. On authentication
            // failure, do not use any plaintext; JCE throws AEADBadTagException.
            return cipher.doFinal(ciphertextAndTag);
        } finally {
            Arrays.fill(aesKey, (byte) 0);
            Arrays.fill(nonce, (byte) 0);
            Arrays.fill(ciphertextAndTag, (byte) 0);
        }
    }

    /**
     * RFC 5869 HKDF using HMAC-SHA-256.
     *
     * HKDF consists of:
     *   Extract: PRK = HMAC(salt, IKM)
     *   Expand:  T(i) = HMAC(PRK, T(i-1) || info || i)
     *
     * This implementation only derives a 16-byte AES-128 key, but implements
     * the general expand loop up to HKDF's RFC 5869 maximum output length.
     *
     * @param ikm input keying material
     * @param salt public HKDF salt
     * @param info application-specific context string
     * @param outputLength requested output length in bytes
     * @return derived key material
     */
    private static byte[] hkdfSha256(
            byte[] ikm, byte[] salt, byte[] info, int outputLength)
            throws GeneralSecurityException {
        requireNonNull(ikm, "ikm");
        requireNonNull(salt, "salt");
        requireNonNull(info, "info");

        if (outputLength < 1
                || outputLength > 255 * HKDF_HASH_LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid HKDF output length.");
        }

        // HKDF-Extract: a salt of HMAC's hash length is recommended. RFC 5869
        // defines an all-zero hash-length salt when the supplied salt is empty.
        byte[] effectiveSalt = salt.length == 0
                ? new byte[HKDF_HASH_LENGTH_BYTES]
                : salt.clone();

        byte[] pseudorandomKey = hmacSha256(effectiveSalt, ikm);
        Arrays.fill(effectiveSalt, (byte) 0);

        try {
            // HKDF-Expand.
            byte[] output = new byte[outputLength];
            byte[] previousBlock = new byte[0];
            int outputOffset = 0;

            for (int blockIndex = 1; outputOffset < outputLength; blockIndex++) {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(pseudorandomKey, "HmacSHA256"));
                mac.update(previousBlock);
                mac.update(info);
                mac.update((byte) blockIndex);
                byte[] currentBlock = mac.doFinal();

                int bytesToCopy = Math.min(
                        currentBlock.length, outputLength - outputOffset);
                System.arraycopy(currentBlock, 0, output, outputOffset, bytesToCopy);
                outputOffset += bytesToCopy;

                Arrays.fill(previousBlock, (byte) 0);
                previousBlock = currentBlock;
            }

            Arrays.fill(previousBlock, (byte) 0);
            return output;
        } finally {
            Arrays.fill(pseudorandomKey, (byte) 0);
        }
    }

    /** Computes HMAC-SHA-256(key, data). */
    private static byte[] hmacSha256(byte[] key, byte[] data)
            throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /** Rejects null inputs with a clear programming error. */
    private static void requireNonNull(byte[] value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null.");
        }
    }
}