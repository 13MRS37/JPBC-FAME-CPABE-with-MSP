package mrs.policy;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

import java.util.HashSet;
import java.util.Set;

/**
 * PolicyParserFacade
 *
 * Provides the public entry point for converting an
 * access policy string into the corresponding
 * Abstract Syntax Tree (AST).
 *
 * Internally, this class orchestrates:
 *
 *      String
 *          â†“
 *      Lexer
 *          â†“
 *      Parser
 *          â†“
 *      Parse Tree
 *          â†“
 *      PolicyTreeBuilder
 *          â†“
 *      PolicyNode
 *
 * The remaining project components never interact
 * directly with ANTLR.
 */
public final class PolicyParserFacade {

    /**
     * Utility class.
     */
    private PolicyParserFacade() {

    }

    /**
     * Parses an access policy and returns its AST.
     *
     * @param policy Access policy string.
     * @return Root node of the AST.
     * @throws IllegalArgumentException if the policy is null,
     * empty, or syntactically invalid.
     */
    public static PolicyNode parse(String policy) {

        if (policy == null || policy.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Policy must not be null or empty.");
        }

        

        PolicyErrorListener errorListener =
        new PolicyErrorListener();

        PolicyLexer lexer =
                new PolicyLexer(
                        CharStreams.fromString(policy));
        
        
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens =
                new CommonTokenStream(lexer);

        PolicyParser parser =
                new PolicyParser(tokens);
        
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        PolicyParser.PolicyContext tree =
                parser.policy();
        
        if (errorListener.hasError()) {
            throw new IllegalArgumentException(
                    "Invalid access policy: "
                    + errorListener.getErrorMessage());
        }

        PolicyTreeBuilder builder =
        new PolicyTreeBuilder();

        PolicyNode root = builder.visit(tree);
        validateUniqueAttributes(root, new HashSet<String>());
        return root;
    }

        /**
         * Validates that every attribute occurs at most once in the entire AST.
         *
         * @param node current AST node
         * @param seen attributes encountered earlier in the traversal
         * @throws IllegalArgumentException if an attribute occurs more than once
         */
        private static void validateUniqueAttributes(
                PolicyNode node,
                Set<String> seen) {

            if (node == null) {
                throw new IllegalArgumentException(
                        "Unsupported null node in access policy AST.");
            }

            if (node instanceof AttributeNode) {
                String attribute = ((AttributeNode) node).getAttribute();

                if (!seen.add(attribute)) {
                    throw new IllegalArgumentException(
                            "Duplicate attribute in policy: " + attribute);
                }

                return;
            }

            if (node instanceof AndNode || node instanceof OrNode) {
                BinaryNode binaryNode = (BinaryNode) node;

                validateUniqueAttributes(binaryNode.getLeft(), seen);
                validateUniqueAttributes(binaryNode.getRight(), seen);

                return;
            }

            throw new IllegalArgumentException(
                    "Unsupported policy node type: "
                    + node.getClass().getName());
        }

        /**
        * Collects lexical and syntactic errors produced by ANTLR.
        */
        private static final class PolicyErrorListener
            extends BaseErrorListener {

        private boolean error;
        private String errorMessage;

        @Override
        public void syntaxError(
                org.antlr.v4.runtime.Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException e) {

            if (!error) {
                error = true;
                errorMessage =
                        "line " + line
                        + ":" + charPositionInLine
                        + " " + msg;
            }
        }

        public boolean hasError() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}