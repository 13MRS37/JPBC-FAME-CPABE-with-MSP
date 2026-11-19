
package mrs.tests;

import mrs.crypto.AESCoder;

import java.security.GeneralSecurityException;
import java.util.Arrays;

public class AESCoderSecurityTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void expectDecryptionFailure(
            String name,
            byte[] key,
            byte[] envelope,
            byte[] aad) throws Exception {

        try {
            byte[] plaintext = AESCoder.decrypt(key, envelope, aad);

            // If decryption succeeds, this test must fail.
            Arrays.fill(plaintext, (byte) 0);
            failed++;
            System.out.println("[FAIL] " + name
                    + " Ã¢â‚¬â€ decryption unexpectedly succeeded.");

        } catch (GeneralSecurityException expected) {
            passed++;
            System.out.println("[PASS] " + name
                    + " Ã¢â‚¬â€ decryption rejected.");
        }
    }

    private static byte[] flipByte(byte[] input, int index) {
        byte[] copy = input.clone();
        copy[index] ^= 0x01;
        return copy;
    }

    private static void testEmptyPlaintext(
            byte[] key, byte[] aad) throws Exception {

        byte[] emptyPlaintext = new byte[0];
        byte[] envelope = AESCoder.encrypt(key, emptyPlaintext, aad);
        byte[] recovered = AESCoder.decrypt(key, envelope, aad);

        if (recovered.length == 0) {
            passed++;
            System.out.println("[PASS] Empty plaintext round-trip.");
        } else {
            failed++;
            System.out.println("[FAIL] Empty plaintext round-trip.");
        }

        // Minimum-size envelope: version + 12-byte nonce + 16-byte tag.
        // The tag remains invalid because these bytes are arbitrary.
        byte[] invalidTagEnvelope = new byte[29];
        invalidTagEnvelope[0] = 0x01;

        expectDecryptionFailure(
                "Minimum-size envelope with invalid tag",
                key,
                invalidTagEnvelope,
                aad);
    }

    private static void expectMalformedEnvelopeRejection(
            String name,
            byte[] key,
            byte[] envelope,
            byte[] aad) throws Exception {

        try {
            byte[] plaintext = AESCoder.decrypt(key, envelope, aad);

            Arrays.fill(plaintext, (byte) 0);
            failed++;
            System.out.println("[FAIL] " + name
                    + " Ã¢â‚¬â€ malformed input was accepted.");

        } catch (GeneralSecurityException expected) {
            passed++;
            System.out.println("[PASS] " + name
                    + " Ã¢â‚¬â€ malformed input rejected.");
        }
    }


    private static void testMalformedEnvelopes(
            byte[] key, byte[] aad) throws Exception {

        expectMalformedEnvelopeRejection(
                "Empty envelope",
                key,
                new byte[0],
                aad);

        expectMalformedEnvelopeRejection(
                "Envelope shorter than minimum",
                key,
                new byte[28],
                aad);

        byte[] unknownVersion = new byte[29];
        unknownVersion[0] = 0x02;

        expectMalformedEnvelopeRejection(
                "Unsupported format version",
                key,
                unknownVersion,
                aad);
    }


    public static void main(String[] args) throws Exception {
        byte[] key = "FAME-test-key-material-01".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        byte[] wrongKey = "FAME-test-key-material-02".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        byte[] plaintext = "Security test plaintext".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        byte[] aad = "A AND B".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        byte[] wrongAad = "A OR B".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);

        byte[] envelope = AESCoder.encrypt(key, plaintext, aad);

        // Baseline: valid inputs must recover the original plaintext.
        byte[] recovered = AESCoder.decrypt(key, envelope, aad);

        if (Arrays.equals(plaintext, recovered)) {
            passed++;
            System.out.println("[PASS] Valid ciphertext decrypts correctly.");
        } else {
            failed++;
            System.out.println("[FAIL] Valid ciphertext did not decrypt correctly.");
        }

        // Envelope layout: version (1) || nonce (12) || ciphertext || tag (16).
        int nonceStart = 1;
        int ciphertextStart = 1 + 12;
        int tagStart = envelope.length - 16;

        expectDecryptionFailure(
                "Modified nonce",
                key,
                flipByte(envelope, nonceStart),
                aad);

        expectDecryptionFailure(
                "Modified ciphertext",
                key,
                flipByte(envelope, ciphertextStart),
                aad);

        expectDecryptionFailure(
                "Modified authentication tag",
                key,
                flipByte(envelope, tagStart),
                aad);

        expectDecryptionFailure(
                "Wrong AAD",
                key,
                envelope,
                wrongAad);

        expectDecryptionFailure(
                "Wrong key",
                wrongKey,
                envelope,
                aad);
        
        testMalformedEnvelopes(key, aad);
        testEmptyPlaintext(key, aad);

        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Result: "
                + (failed == 0 ? "ALL TESTS PASSED" : "TESTS FAILED"));

        if (failed != 0) {
            throw new AssertionError("AES-GCM security tests failed.");
        }
    }
}