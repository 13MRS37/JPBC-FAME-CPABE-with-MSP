package mrs.policy;

/**
 * Logical AND operator.
 *
 * Both left and right subtree must be satisfied.
 */
public class AndNode extends BinaryNode {

    public AndNode(PolicyNode left,
                   PolicyNode right) {

        super(left, right);

    }

    @Override
    public String toString() {

        return "("
                + left.toString()
                + " AND "
                + right.toString()
                + ")";

    }

}