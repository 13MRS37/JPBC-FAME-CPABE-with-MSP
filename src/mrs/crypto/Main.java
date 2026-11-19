package mrs.crypto;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Main {
    private static final int RUNS = 10;
    private static final int WARMUP_RUNS = 3;
    private static final byte[] MESSAGE =
            "FAME nested-policy benchmark".getBytes(StandardCharsets.UTF_8);

    private static final TestCase[] TESTS = {
        new TestCase("1.Six-way AND - all attributes", "A AND B AND C AND D AND E AND F", new String[]{"A", "B", "C", "D", "E", "F"}, true),
        new TestCase("2.Six-way AND - missing F", "A AND B AND C AND D AND E AND F", new String[]{"A", "B", "C", "D", "E"}, false),
        new TestCase("3.Six-way OR - attribute E", "A OR B OR C OR D OR E OR F", new String[]{"E"}, true),
        new TestCase("4.Duplicate attribute - rejected by parser", "A OR (B AND A)", new String[]{"A", "B"}, false),
        new TestCase("Duplicate attribute - AND", "A AND A", new String[]{"A"}, false),
        new TestCase("Duplicate attribute - OR", "A OR A", new String[]{"A"}, false),
        new TestCase("Six-way OR - no matching attribute", "A OR B OR C OR D OR E OR F", new String[]{"G"}, false),
        new TestCase("Nested policy - satisfied", "A AND (B OR C) AND (D OR E) AND F", new String[]{"A", "C", "D", "F"}, true),
        new TestCase("Nested policy - missing D/E", "A AND (B OR C) AND (D OR E) AND F", new String[]{"A", "C", "F"}, false),
        new TestCase("Three alternative branches - satisfied", "(A AND B) OR (C AND D) OR (E AND F)", new String[]{"C", "D"}, true),
        new TestCase("Three alternative branches - incomplete branch", "(A AND B) OR (C AND D) OR (E AND F)", new String[]{"A", "C"}, false),
        new TestCase("Three OR groups - satisfied", "(A OR B) AND (C OR D) AND (E OR F)", new String[]{"A", "D", "F"}, true),
        new TestCase("Three OR groups - missing third group", "(A OR B) AND (C OR D) AND (E OR F)", new String[]{"A", "B", "C"}, false),
        new TestCase("Deep nested policy - all requirements satisfied", "((A AND B) OR C) AND (D OR (E AND F))", new String[]{"A", "B", "E", "F"}, true),
        new TestCase("Deep nested policy - alternative branches satisfied", "((A AND B) OR C) AND (D OR (E AND F))", new String[]{"C", "D"}, true),
        new TestCase("Deep nested policy - first group missing", "((A AND B) OR C) AND (D OR (E AND F))", new String[]{"A", "D"}, false),
        new TestCase("Deep nested policy - second group incomplete", "((A AND B) OR C) AND (D OR (E AND F))", new String[]{"A", "B", "E"}, false),
        new TestCase("Three AND branches - first branch satisfied", "(A AND B AND C) OR (D AND E) OR (F AND G)", new String[]{"A", "B", "C"}, true),
        new TestCase("Three AND branches - second branch satisfied", "(A AND B AND C) OR (D AND E) OR (F AND G)", new String[]{"D", "E"}, true),
        new TestCase("Three AND branches - third branch satisfied", "(A AND B AND C) OR (D AND E) OR (F AND G)", new String[]{"F", "G"}, true),
        new TestCase("Three AND branches - mixed incomplete branches", "(A AND B AND C) OR (D AND E) OR (F AND G)", new String[]{"A", "D", "F"}, false),
        new TestCase("Three AND branches - one missing attribute", "(A AND B AND C) OR (D AND E) OR (F AND G)", new String[]{"A", "B", "D", "E", "F"}, true),
        new TestCase("Four OR groups - one attribute per group", "(A OR B) AND (C OR D) AND (E OR F) AND (G OR H)", new String[]{"A", "D", "E", "H"}, true),
        new TestCase("Four OR groups - multiple alternatives", "(A OR B) AND (C OR D) AND (E OR F) AND (G OR H)", new String[]{"A", "B", "C", "D", "F", "G"}, true),
        new TestCase("Four OR groups - fourth group missing", "(A OR B) AND (C OR D) AND (E OR F) AND (G OR H)", new String[]{"A", "C", "E"}, false),
        new TestCase("Four OR groups - third group missing", "(A OR B) AND (C OR D) AND (E OR F) AND (G OR H)", new String[]{"B", "D", "G"}, false),
        new TestCase("Complex nested policy - satisfied through left branches", "((A OR B) AND (C OR (D AND E))) OR ((F AND G) AND (H OR I))", new String[]{"B", "C"}, true),
        new TestCase("Complex nested policy - satisfied through right branch", "((A OR B) AND (C OR (D AND E))) OR ((F AND G) AND (H OR I))", new String[]{"F", "G", "I"}, true),
        new TestCase("Complex nested policy - incomplete left branch", "((A OR B) AND (C OR (D AND E))) OR ((F AND G) AND (H OR I))", new String[]{"A", "D"}, false),
        new TestCase("Complex nested policy - incomplete right branch", "((A OR B) AND (C OR (D AND E))) OR ((F AND G) AND (H OR I))", new String[]{"F", "H"}, false),
        new TestCase("Large policy - first complex branch", "((A AND B) OR (C AND D)) AND (E OR F) OR ((G AND H) OR (I AND J))", new String[]{"A", "B", "E"}, true),
        new TestCase("Large policy - second complex branch", "((A AND B) OR (C AND D)) AND (E OR F) OR ((G AND H) OR (I AND J))", new String[]{"G", "H"}, true),
        new TestCase("Large policy - third alternative within first group", "((A AND B) OR (C AND D)) AND (E OR F) OR ((G AND H) OR (I AND J))", new String[]{"C", "D", "F"}, true),
        new TestCase("Large policy - no complete branch", "((A AND B) OR (C AND D)) AND (E OR F) OR ((G AND H) OR (I AND J))", new String[]{"A", "C", "E", "G"}, false),
        new TestCase("Stress 1 - nested AND/OR, satisfied", "(((A AND B) OR (C AND D)) AND (E OR F)) AND (G OR H)", new String[]{"A", "B", "E", "G"}, true),
        new TestCase("Stress 2 - nested AND/OR, final group missing", "(((A AND B) OR (C AND D)) AND (E OR F)) AND (G OR H)", new String[]{"A", "B", "E"}, false),
        new TestCase("Stress 3 - nested alternative branches, left satisfied", "((A AND (B OR C)) OR (D AND (E OR F))) AND (G OR (H AND I))", new String[]{"A", "C", "G"}, true),
        new TestCase("Stress 4 - nested alternative branches, right satisfied", "((A AND (B OR C)) OR (D AND (E OR F))) AND (G OR (H AND I))", new String[]{"D", "F", "H", "I"}, true),
        new TestCase("Stress 5 - both major branches incomplete", "((A AND (B OR C)) OR (D AND (E OR F))) AND (G OR (H AND I))", new String[]{"D", "E", "H"}, false),
        new TestCase("Stress 6 - deep left branch satisfied", "(((A AND B) OR (C AND D)) AND (E OR (F AND G))) OR ((H AND (I OR J)) AND (K OR (L AND M)))", new String[]{"A", "B", "E"}, true),
        new TestCase("Stress 7 - deep right branch satisfied", "(((A AND B) OR (C AND D)) AND (E OR (F AND G))) OR ((H AND (I OR J)) AND (K OR (L AND M)))", new String[]{"H", "J", "K"}, true),
        new TestCase("Stress 8 - neither deep branch satisfied", "(((A AND B) OR (C AND D)) AND (E OR (F AND G))) OR ((H AND (I OR J)) AND (K OR (L AND M)))", new String[]{"A", "C", "E", "H", "I"}, false),
        new TestCase("Stress 9 - nested AND/OR, alternative satisfied", "((A OR B) AND ((C AND D) OR (E AND (F OR G)))) AND (H OR (I AND J))", new String[]{"B", "E", "F", "I", "J"}, true),
        new TestCase("Stress 10 - nested AND/OR, inner requirement missing", "((A OR B) AND ((C AND D) OR (E AND (F OR G)))) AND (H OR (I AND J))", new String[]{"A", "C", "E", "H", "I"}, false),
    };

    public static void main(String[] args) throws Exception {
        String outputFile = "nested_policy_benchmark_java.csv";
        int passed = 0;
        int failed = 0;

        try (BufferedWriter writer = Files.newBufferedWriter(
                Path.of(outputFile), StandardCharsets.UTF_8)) {
            writer.write("Test_ID,Test_Name,Policy,User_Attributes,Expected_Success,Run,Setup_ms,KeyGen_ms,Encrypt_ms,Decrypt_ms,Total_ms,Outcome,Correct,Error");
            writer.newLine();

            for (int i = 0; i < TESTS.length; i++) {
                TestCase test = TESTS[i];
                System.out.printf("\n[%02d/%02d] %s\nPolicy: %s\nAttributes: %s\n",
                        i + 1, TESTS.length, test.name, test.policy,
                        Arrays.toString(test.attributes));

                for (int w = 0; w < WARMUP_RUNS; w++) {
                    execute(test);
                }

                for (int run = 1; run <= RUNS; run++) {
                    Result r = execute(test);
                    if (r.correct) passed++; else failed++;
                    writer.write(String.format(Locale.US,
                            "%d,\"%s\",\"%s\",\"%s\",%b,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%s,%b,\"%s\"",
                            i + 1, csv(test.name), csv(test.policy),
                            csv(String.join(";", test.attributes)), test.expected,
                            run, r.setupMs, r.keygenMs, r.encryptMs,
                            r.decryptMs, r.totalMs, r.outcome, r.correct,
                            csv(r.error)));
                    writer.newLine();
                    writer.flush();
                    System.out.printf(Locale.US,
                            "Run %d/%d | Setup %.4f | KeyGen %.4f | Encrypt %.4f | Decrypt %.4f | Total %.4f ms | %s | Correct %b%n",
                            run, RUNS, r.setupMs, r.keygenMs, r.encryptMs,
                            r.decryptMs, r.totalMs, r.outcome, r.correct);
                }
            }
        }
        System.out.printf("\nCompleted: %d tests x %d runs = %d measured runs\nPassed: %d | Failed: %d\nCSV: %s\n",
                TESTS.length, RUNS, TESTS.length * RUNS, passed, failed, outputFile);
        if (failed > 0) throw new IllegalStateException("Benchmark correctness failures: " + failed);
    }

    private static Result execute(TestCase test) throws Exception {
        Result r = new Result();
        // Constructor/parameter loading is intentionally outside Setup timing.
        FAME cpabe = new FAME();

        long start = System.nanoTime();
        FAMEMasterKey msk = cpabe.setup();
        r.setupMs = ms(start);

        start = System.nanoTime();
        FAMESecretKey sk = cpabe.keygen(msk, test.attributes);
        r.keygenMs = ms(start);

        FAMECipherText ct;
        start = System.nanoTime();
        try {
            ct = cpabe.encrypt(test.policy, MESSAGE);
            r.encryptMs = ms(start);
            start = System.nanoTime();
            try {
                byte[] recovered = cpabe.decrypt(sk, ct);
                r.decryptMs = ms(start);
                boolean plaintextOK = Arrays.equals(MESSAGE, recovered);
                r.outcome = "DECRYPT_SUCCESS";
                r.correct = test.expected && plaintextOK;
                if (!plaintextOK) r.error = "Recovered plaintext differs";
                else if (!test.expected) r.error = "Unexpected decryption success";
            } catch (IllegalArgumentException e) {
                r.decryptMs = ms(start);
                boolean expectedRejection = !test.expected &&
                        "Policy not satisfied by the user's attributes.".equals(e.getMessage());
                r.outcome = expectedRejection ? "POLICY_NOT_SATISFIED" : "DECRYPT_ERROR";
                r.correct = expectedRejection;
                if (!expectedRejection) r.error = e.toString();
            }
        } catch (IllegalArgumentException e) {
            r.encryptMs = ms(start);
            boolean duplicateRejected = !test.expected && e.getMessage() != null &&
                    e.getMessage().startsWith("Duplicate attribute in policy: ");
            r.outcome = duplicateRejected ? "PARSER_REJECTED_DUPLICATE" : "ENCRYPT_ERROR";
            r.correct = duplicateRejected;
            if (!duplicateRejected) r.error = e.toString();
        }
        r.totalMs = r.setupMs + r.keygenMs + r.encryptMs + r.decryptMs;
        return r;
    }

    private static double ms(long start) { return (System.nanoTime() - start) / 1_000_000.0; }
    private static String csv(String s) { return s.replace("\"", "\"\"").replace("\r", " ").replace("\n", " "); }

    private static final class TestCase {
        final String name, policy; final String[] attributes; final boolean expected;
        TestCase(String name, String policy, String[] attributes, boolean expected) {
            this.name = name; this.policy = policy; this.attributes = attributes; this.expected = expected;
        }
    }
    private static final class Result {
        double setupMs, keygenMs, encryptMs, decryptMs, totalMs;
        String outcome = "NOT_COMPLETED", error = ""; boolean correct;
    }
}
