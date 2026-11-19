package mrs.policy;

/**
 * Base class of every node inside the Access Policy Abstract Syntax Tree (AST).
 *
 * Every access policy is represented as a rooted tree.
 *
 * Leaf nodes:
 *      AttributeNode
 *
 * Internal nodes:
 *      AndNode
 *      OrNode
 *
 * The resulting AST is later consumed by the MSPCompiler in order
 * to construct the corresponding Monotone Span Program (MSP).
 */
public abstract class PolicyNode {

    /**
     * Returns a human-readable representation of the subtree.
     */
    @Override
    public abstract String toString();

}