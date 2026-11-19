
package mrs.tests;

import mrs.msp.MSPCompiler;
import mrs.msp.MSPMatrix;
import mrs.msp.ZrMSPReconstruction;

import java.util.HashSet;
import java.util.Set;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import mrs.policy.PolicyNode;
import mrs.policy.PolicyParserFacade;

public class ZrMSPReconstructionTest {

    private static int passed = 0;
    private static int failed = 0;

    private static Field Zr;

    private static void check(
            String policy,
            String[] userAttributes,
            boolean expected) throws Exception {

        PolicyNode root = PolicyParserFacade.parse(policy);
        MSPMatrix matrix = new MSPCompiler().compile(root);

        Set<String> attributes = new HashSet<>();

        for (String attribute : userAttributes) {
            attributes.add(attribute);
        }

        ZrMSPReconstruction result =
                ZrMSPReconstruction.reconstruct(
                        matrix, attributes, Zr);

        boolean correctStatus =
        result.isSatisfied() == expected;

        boolean correctEquation =
                !result.isSatisfied()
                || verifyReconstruction(matrix, result);

        if (correctStatus && correctEquation) {
            passed++;
            System.out.println(
                    "[PASS] " + policy
                    + " | Attributes: "
                    + attributes
                    + " | Satisfied: "
                    + result.isSatisfied()
                    + " | Equation valid: "
                    + correctEquation);
        } else {
            failed++;
            System.out.println(
                    "[FAIL] " + policy
                    + " | Attributes: "
                    + attributes
                    + " | Expected satisfied: "
                    + expected
                    + " | Actual satisfied: "
                    + result.isSatisfied()
                    + " | Equation valid: "
                    + correctEquation);
        }
    }

    private static boolean verifyReconstruction(
            MSPMatrix matrix,
            ZrMSPReconstruction result) {

        int columns = matrix.getColumnCount();

        java.util.List<Integer> selectedRows =
                result.getSelectedRows();

        Element[] coefficients =
                result.getCoefficients();

        if (selectedRows.size() != coefficients.length) {
            return false;
        }

        // Verify:
        // sum_i lambda_i * M_i = (1, 0, ..., 0)
        for (int column = 0; column < columns; column++) {

            Element sum = Zr.newZeroElement();

            for (int i = 0; i < selectedRows.size(); i++) {

                int rowIndex = selectedRows.get(i);
                int[] row = matrix.getRow(rowIndex);

                Element entry =
                        Zr.newElement(row[column]);

                Element term =
                        coefficients[i].duplicate().mul(entry);

                sum.add(term);
            }

            Element expected =
                    column == 0
                            ? Zr.newOneElement()
                            : Zr.newZeroElement();

            if (!sum.isEqual(expected)) {
                System.out.println(
                        "    Invalid equation at column "
                        + column
                        + ": expected "
                        + expected
                        + ", got "
                        + sum);
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws Exception {

        Pairing pairing =
                PairingFactory.getPairing("MNT224");

        Zr = pairing.getZr();

        System.out.println(
                "=== Zr MSP Reconstruction Tests ===");

        // AND
        check("A AND B",
                new String[]{"A", "B"}, true);

        check("A AND B",
                new String[]{"A"}, false);

        // OR
        check("A OR B",
                new String[]{"A"}, true);

        check("A OR B",
                new String[]{"C"}, false);

        // Nested policy
        check("A AND (B OR C)",
                new String[]{"A", "C"}, true);

        check("A AND (B OR C)",
                new String[]{"A"}, false);

        // Multiple independent branches
        check("(A OR B) AND (C OR D)",
                new String[]{"A", "D"}, true);

        check("(A OR B) AND (C OR D)",
                new String[]{"A", "B"}, false);

        // Deeply nested AND/OR combinations
        check("((A AND B) OR C) AND D",
                new String[]{"A", "B", "D"}, true);

        check("((A AND B) OR C) AND D",
                new String[]{"C", "D"}, true);

        check("((A AND B) OR C) AND D",
                new String[]{"A", "D"}, false);

        check("(A AND B) OR (C AND D)",
                new String[]{"A", "D"}, false);

        check("(A AND B) OR (C AND D)",
                new String[]{"C", "D"}, true);

        System.out.println();
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println(
                "Result: "
                + (failed == 0
                   ? "ALL TESTS PASSED"
                   : "TESTS FAILED"));
    }
}