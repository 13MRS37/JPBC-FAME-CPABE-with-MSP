
package mrs.tests;

import mrs.crypto.AESCoder;

import java.lang.reflect.Method;
import java.util.HexFormat;

public class HKDFTest {

    public static void main(String[] args) throws Exception {
        byte[] ikm = HexFormat.of().parseHex(
                "0b".repeat(22));

        byte[] salt = HexFormat.of().parseHex(
                "000102030405060708090a0b0c");

        byte[] info = HexFormat.of().parseHex(
                "f0f1f2f3f4f5f6f7f8f9");

        String expected =
                "3cb25f25faacd57a90434f64d0362f2a" +
                "2d2d0a90cf1a5a4c5db02d56ecc4c5bf" +
                "34007208d5b887185865";

        Method method = AESCoder.class.getDeclaredMethod(
                "hkdfSha256",
                byte[].class,
                byte[].class,
                byte[].class,
                int.class);

        method.setAccessible(true);

        byte[] actual = (byte[]) method.invoke(
                null, ikm, salt, info, 42);

        String actualHex = HexFormat.of().formatHex(actual);

        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + actualHex);
        System.out.println("Result:   " +
                (expected.equals(actualHex) ? "PASS" : "FAIL"));

        if (!expected.equals(actualHex)) {
            throw new AssertionError("HKDF RFC 5869 test failed");
        }
    }
}