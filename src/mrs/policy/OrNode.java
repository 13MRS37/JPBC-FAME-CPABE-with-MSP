package mrs.policy;

/**
 * Logical OR operator.
 *
 * Either subtree may satisfy the policy.
 */
public class OrNode extends BinaryNode {

    public OrNode(PolicyNode left,
                  PolicyNode right) {

        super(left, right);

    }

    @Override
    public String toString() {

        return "("
                + left.toString()
                + " OR "
                + right.toString()
                + ")";

    }

}