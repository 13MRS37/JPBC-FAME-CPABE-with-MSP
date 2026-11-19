package mrs.policy;

/**
 * Base class for every binary logical operator in the policy AST.
 *
 * Binary nodes always have two children:
 *  - left subtree
 *  - right subtree
 *
 * This class is extended by:
 *  - AndNode
 *  - OrNode
 */
public abstract class BinaryNode extends PolicyNode {

    protected final PolicyNode left;
    protected final PolicyNode right;

    public BinaryNode(PolicyNode left, PolicyNode right) {
        this.left = left;
        this.right = right;
    }

    public PolicyNode getLeft() {
        return left;
    }

    public PolicyNode getRight() {
        return right;
    }
}
