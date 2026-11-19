package mrs.policy;

public class PolicyParserTest {

    public static void main(String[] args) {

        String[] validPolicies = {
                "A",
                "A AND B",
                "A OR B",
                "A AND (B OR C)",
                "(A AND B) OR C",
                "((A AND B) OR C)",
                "A AND a",
                "Doctor AND (Hospital OR Nurse)"
        };

        String[] invalidPolicies = {
                "A AND",
                "OR B",
                "A AND OR B",
                "()",
                "(A AND)",
                "(A OR)",
                "A-B",
                "Department:HR",
                "User Name",
                "A AND A",
                "A OR (B AND A)"
        };

        System.out.println("===== VALID POLICIES =====");

        for (String policy : validPolicies) {
            try {
                PolicyNode ast = PolicyParserFacade.parse(policy);

                System.out.println(
                        "[PASS] " + policy + " -> " + ast
                );

            } catch (Exception e) {

                System.out.println(
                        "[FAIL] " + policy
                        + " -> " + e.getMessage()
                );
            }
        }

        System.out.println();
        System.out.println("===== INVALID POLICIES =====");

        for (String policy : invalidPolicies) {
            try {
                PolicyNode ast = PolicyParserFacade.parse(policy);

                System.out.println(
                        "[FAIL] " + policy
                        + " was accepted -> " + ast
                );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "[PASS] " + policy
                        + " rejected -> " + e.getMessage()
                );
            }
        }
    }
}