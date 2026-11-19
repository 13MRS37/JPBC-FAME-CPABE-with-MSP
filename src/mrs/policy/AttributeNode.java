package mrs.policy;

/**
 * Leaf node representing one attribute.
 *
 * Example:
 *
 *      Doctor
 *      Department_HR
 *      Role_Admin
 *      123Role
 */
public class AttributeNode extends PolicyNode {

    private final String attribute;

    public AttributeNode(String attribute) {

        this.attribute = attribute;

    }

    public String getAttribute() {

        return attribute;

    }

    @Override
    public String toString() {

        return attribute;

    }

}