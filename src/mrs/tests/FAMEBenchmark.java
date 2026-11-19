
package mrs.tests;

import mrs.crypto.FAME;
import mrs.crypto.FAMECipherText;
import mrs.crypto.FAMEMasterKey;
import mrs.crypto.FAMESecretKey;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

public class FAMEBenchmark {

    private static final int MAX_ATTRIBUTES = 100;
    private static final int RUNS = 10;
    private static final int WARMUP_RUNS = 3;

    private static final byte[] MESSAGE =
            "FAME AND Benchmark Test".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws Exception {

        FAME cpabe = new FAME();

        String outputFile = "AND_benchmark_results_java.csv";

        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(outputFile),
                StandardCharsets.UTF_8)) {

            // CSV header
            writer.write(
                    "Attributes,Run,Setup_ms,KeyGen_ms,"
                    + "Encrypt_ms,Decrypt_ms,Total_ms,Correct"
            );
            writer.newLine();

            // Test policies from 1 to 100 attributes
            for (int n = 1; n <= MAX_ATTRIBUTES; n++) {

                // Generate attributes
                String[] attributes = new String[n];

                for (int i = 0; i < n; i++) {
                    attributes[i] = "ATTR" + (i + 1);
                }

                // Construct AND-only policy
                String policy = String.join(" AND ", attributes);

                System.out.println("\n====================================");
                System.out.println("ATTRIBUTES: " + n);
                System.out.println("POLICY: " + policy);
                System.out.println("====================================");

                // JVM warm-up runs (not saved)
                for (int w = 0; w < WARMUP_RUNS; w++) {
                    executeTest(cpabe, attributes, policy);
                }

                // Actual benchmark runs
                for (int run = 1; run <= RUNS; run++) {

                    BenchmarkResult result =
                            executeTest(cpabe, attributes, policy);

                    writer.write(String.format(
                            Locale.US,
                            "%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%b",
                            n,
                            run,
                            result.setupMs,
                            result.keygenMs,
                            result.encryptMs,
                            result.decryptMs,
                            result.totalMs,
                            result.correct
                    ));

                    writer.newLine();
                    writer.flush();

                    System.out.printf(
                            "Run %d/%d | Setup: %.4f ms | "
                            + "KeyGen: %.4f ms | Encrypt: %.4f ms | "
                            + "Decrypt: %.4f ms | Total: %.4f ms | "
                            + "Correct: %b%n",
                            run,
                            RUNS,
                            result.setupMs,
                            result.keygenMs,
                            result.encryptMs,
                            result.decryptMs,
                            result.totalMs,
                            result.correct
                    );
                }
            }
        }

        System.out.println("\n====================================");
        System.out.println("BENCHMARK COMPLETED");
        System.out.println("Results saved to: " + outputFile);
        System.out.println("Total measured runs: "
                + (MAX_ATTRIBUTES * RUNS));
        System.out.println("====================================");
    }

    private static BenchmarkResult executeTest(
            FAME cpabe,
            String[] attributes,
            String policy) throws Exception {

        BenchmarkResult result = new BenchmarkResult();

        // 1. SETUP
        long start = System.nanoTime();

        FAMEMasterKey msk = cpabe.setup();

        result.setupMs = elapsedMilliseconds(start);

        // 2. KEY GENERATION
        start = System.nanoTime();

        FAMESecretKey skey =
                cpabe.keygen(msk, attributes);

        result.keygenMs = elapsedMilliseconds(start);

        // 3. ENCRYPTION
        start = System.nanoTime();

        FAMECipherText ciphertext =
                cpabe.encrypt(policy, MESSAGE);

        result.encryptMs = elapsedMilliseconds(start);

        // 4. DECRYPTION
        start = System.nanoTime();

        byte[] decrypted =
                cpabe.decrypt(skey, ciphertext);

        result.decryptMs = elapsedMilliseconds(start);

        // 5. TOTAL TIME
        result.totalMs =
                result.setupMs
                + result.keygenMs
                + result.encryptMs
                + result.decryptMs;

        // 6. CORRECTNESS
        result.correct = Arrays.equals(MESSAGE, decrypted);

        return result;
    }

    private static double elapsedMilliseconds(long start) {
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static class BenchmarkResult {

        double setupMs;
        double keygenMs;
        double encryptMs;
        double decryptMs;
        double totalMs;

        boolean correct;
    }
}
