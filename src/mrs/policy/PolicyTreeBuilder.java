package mrs.policy;

/**
 * PolicyTreeBuilder
 *
 * Converts the Parse Tree produced by ANTLR into the
 * Abstract Syntax Tree (AST) used by the MSP compiler.
 *
 * Responsibilities:
 *
 *  - Visit every parser node.
 *  - Create the corresponding AST node.
 *  - Preserve the logical structure of the access policy.
 *
 * This class performs NO semantic validation and NO MSP generation.
 * Its only responsibility is Parse Tree â†’ AST conversion.
 */
public class PolicyTreeBuilder extends PolicyBaseVisitor<PolicyNode> {

    /**
     * Visits the root policy node.
     *
     * According to the grammar:
     *
     *      policy : expr EOF ;
     *
     * every valid access policy consists of exactly one root
     * expression followed by the end of the input.
     *
     * Therefore, this method simply delegates the traversal
     * to the root expression of the parse tree.
     *
     * ANTLR will automatically dispatch the traversal to the
     * appropriate visit method depending on the expression type
     * (AND, OR, ATTRIBUTE or Parenthesized Expression).
     *
     * @param ctx Root parser context.
     * @return Root node of the constructed Abstract Syntax Tree (AST).
     */
    @Override
    public PolicyNode visitPolicy(PolicyParser.PolicyContext ctx) {

        return visit(ctx.expr());

    }

        /**
     * Visits an attribute expression.
     *
     * According to the grammar:
     *
     *      expr : ATTRIBUTE # AttrExpr
     *
     * Every attribute is represented as a leaf node in the
     * Abstract Syntax Tree (AST).
     *
     * The attribute identifier is extracted exactly as it appears
     * in the access policy and stored inside an AttributeNode.
     *
     * Examples:
     *
     *      Doctor
     *      Department_HR
     *      123Role
     *
     * @param ctx Attribute expression parser context.
     * @return AttributeNode representing the parsed attribute.
     */
    @Override
    public PolicyNode visitAttrExpr(PolicyParser.AttrExprContext ctx) {

        /*
         * Read the attribute identifier directly from the parser.
         */
        String attribute = ctx.ATTRIBUTE().getText();

        /*
         * Create the corresponding AST leaf node.
         */
        return new AttributeNode(attribute);

    }

    /**
     * Visits a parenthesized expression.
     *
     * According to the grammar:
     *
     *      expr : '(' expr ')' # ParenExpr
     *
     * Parentheses are syntactic constructs that define parsing
     * precedence only. They do not represent logical operators
     * and therefore do not appear in the Abstract Syntax Tree (AST).
     *
     * Instead of creating a new AST node, the builder simply
     * continues the traversal by visiting the enclosed expression.
     *
     * Consequently, the following expressions produce identical ASTs:
     *
     *      A
     *      (A)
     *      ((A))
     *      (((A)))
     *
     * @param ctx Parenthesized expression parser context.
     * @return The AST node corresponding to the enclosed expression.
     */
    @Override
    public PolicyNode visitParenExpr(PolicyParser.ParenExprContext ctx) {

        /*
         * Parentheses are ignored in the AST.
         * Continue recursively with the enclosed expression.
         */
        return visit(ctx.expr());

    }

    /**
     * Visits a logical AND expression.
     *
     * According to the grammar:
     *
     *      expr : expr AND expr # AndExpr
     *
     * A logical AND represents an internal node of the AST with
     * exactly two child subtrees.
     *
     * The visitor recursively constructs the left and right
     * subtrees before combining them into a single AndNode.
     *
     * Example:
     *
     *      A AND B
     *
     *              AND
     *             /   \
     *            A     B
     *
     * @param ctx Logical AND parser context.
     * @return AndNode representing the logical conjunction.
     */
    @Override
    public PolicyNode visitAndExpr(PolicyParser.AndExprContext ctx) {

        /*
         * Recursively construct the left subtree.
         */
        PolicyNode left = visit(ctx.expr(0));

        /*
         * Recursively construct the right subtree.
         */
        PolicyNode right = visit(ctx.expr(1));

        /*
         * Create the corresponding AST node.
         */
        return new AndNode(left, right);

    }

        /**
     * Visits a logical OR expression.
     *
     * According to the grammar:
     *
     *      expr : expr OR expr # OrExpr
     *
     * A logical OR represents an internal node of the AST with
     * exactly two child subtrees.
     *
     * The visitor recursively constructs the left and right
     * subtrees before combining them into a single OrNode.
     *
     * Example:
     *
     *      A OR B
     *
     *              OR
     *             /  \
     *            A    B
     *
     * More complex example:
     *
     *      (A AND B) OR (C OR D)
     *
     *                    OR
     *                  /     \
     *               AND       OR
     *              /   \     /  \
     *             A     B   C    D
     *
     * @param ctx Logical OR parser context.
     * @return OrNode representing the logical disjunction.
     */
    @Override
    public PolicyNode visitOrExpr(PolicyParser.OrExprContext ctx) {

        /*
         * Recursively construct the left subtree.
         */
        PolicyNode left = visit(ctx.expr(0));

        /*
         * Recursively construct the right subtree.
         */
        PolicyNode right = visit(ctx.expr(1));

        /*
         * Create the corresponding AST node.
         */
        return new OrNode(left, right);

    }

}