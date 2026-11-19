
package mrs.tests;

import mrs.crypto.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MSPIntegrationTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void test(
            FAME fame,
            FAMEMasterKey msk,
            String policy,
            String[] userAttributes,
            boolean shouldSucceed) {

        String testName =
                "Policy: " + policy
                + " | User attributes: "
                + Arrays.toString(userAttributes);

        try {
            FAMESecretKey sk = fame.keygen(msk, userAttributes);

            byte[] plaintext =
                    "FAME MSP integration test"
                    .getBytes(StandardCharsets.UTF_8);

            FAMECipherText ciphertext =
                    fame.encrypt(policy, plaintext);

            byte[] decrypted = fame.decrypt(sk, ciphertext);

            boolean correct =
                    Arrays.equals(plaintext, decrypted);

            if (shouldSucceed && correct) {
                passed++;
                System.out.println("[PASS] " + testName);
            } else {
                failed++;
                System.out.println("[FAIL] " + testName);
                System.out.println(
                        "Expected successful decryption: "
                        + shouldSucceed);
                System.out.println(
                        "Plaintext recovered correctly: "
                        + correct);
            }

        
        } catch (IllegalArgumentException e) {

            if (!shouldSucceed
                    && "Policy not satisfied by the user's attributes."
                            .equals(e.getMessage())) {
                passed++;
                System.out.println(
                        "[PASS] Access denied as expected: "
                        + testName);
            } else {
                failed++;
                System.out.println(
                        "[FAIL] " + testName
                        + " | Unexpected IllegalArgumentException: "
                        + e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {

            failed++;
            System.out.println(
                    "[FAIL] " + testName
                    + " | Unexpected exception: " + e);
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {

        FAME fame = new FAME();
        FAMEMasterKey msk = fame.setup();

        System.out.println("=== FAME MSP Integration Tests ===");

        // AND: both attributes are required.
        test(fame, msk,
                "A AND B",
                new String[]{"A", "B"},
                true);

        test(fame, msk,
                "A AND B",
                new String[]{"A"},
                false);

        // OR: either attribute should be sufficient.
        test(fame, msk,
                "A OR B",
                new String[]{"A"},
                true);

        test(fame, msk,
                "A OR B",
                new String[]{"B"},
                true);

        test(fame, msk,
                "A OR B",
                new String[]{"C"},
                false);

        // Nested policy: A AND (B OR C).
        test(fame, msk,
                "A AND (B OR C)",
                new String[]{"A", "B"},
                true);

        test(fame, msk,
                "A AND (B OR C)",
                new String[]{"A", "C"},
                true);

        test(fame, msk,
                "A AND (B OR C)",
                new String[]{"A"},
                false);
        
        // Additional edge-case tests.
        
        /*  Repeated attribute in separate MSP rows.
        test(fame, msk,
                "A OR (A AND B)",
                new String[]{"A"},
                true);
        */
       
        // Nested OR/AND policy with unique attributes.
        test(fame, msk,
                "A OR (B AND C)",
                new String[]{"A"},
                true);


        test(fame, msk,
                "(A OR B) AND (C OR D)",
                new String[]{"A", "D"},
                true);

        test(fame, msk,
                "(A OR B) AND (C OR D)",
                new String[]{"A", "B", "C"},
                true);

        test(fame, msk,
                "(A OR B) AND (C OR D)",
                new String[]{"A", "B"},
                false);


        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println(
                "Result: "
                + (failed == 0 ? "ALL TESTS PASSED" : "TESTS FAILED"));
    }
}