package mrs.msp;

import mrs.policy.*;

/**
 * MSPCompiler
 *
 * Converts an access policy Abstract Syntax Tree (AST)
 * into an equivalent Monotone Span Program (MSP).
 *
 * Input:
 *      Policy AST
 *
 * Output:
 *      MSP Matrix (M, Ï)
 *
 * This implementation follows the recursive top-down
 * construction described in the MSP/LSSS literature.
 *
 * Each recursive call receives a target vector that
 * the current subtree must reconstruct.
 *
 * The compiler itself performs no verification.
 * Mathematical verification of the resulting MSP is
 * delegated to the MSPVerifier component.
 */
public class MSPCompiler {

    /**
     * Compiles an access policy into an MSP matrix.
     *
     * The complete access policy must reconstruct the
     * initial target vector:
     *
     *      eâ‚ = [1]
     *
     * Compilation starts from the root of the AST and
     * recursively traverses every subtree.
     *
     * @param root Root node of the policy AST.
     * @return Complete MSP matrix.
     */
    public MSPMatrix compile(PolicyNode root) {

        /*
         * Initial target vector.
         *
         * eâ‚ = [1]
         */
        int[] targetVector = {1};

        /*
        * Empty MSP matrix.
        */
        MSPMatrix matrix = new MSPMatrix();

        /*
        * Compilation context shared by all recursive calls.
        */
        MSPContext context = new MSPContext();

        /*
        * Start recursive compilation.
        */
        compileNode(root, targetVector, matrix, context);

        /*
        * Appendix G (Lewko-Waters):
        *
        * After every leaf has been assigned its vector,
        * all leaf vectors become rows of the LSSS matrix.
        * If some rows are shorter than others, they are
        * padded with trailing zeroes so that every row has
        * identical dimension.
        */
        normalizeRows(matrix, context.getDimension());

        return matrix;

    }

    /**
     * Recursively compiles one subtree of the policy AST.
     *
     * Every subtree receives the target vector that must
     * be reconstructed by the corresponding MSP rows.
     *
     * Depending on the node type, compilation is delegated
     * to the appropriate construction rule.
     *
     *      AttributeNode
     *          â†“
     *      compileAttribute(...)
     *
     *      AndNode
     *          â†“
     *      compileAnd(...)
     *
     *      OrNode
     *          â†“
     *      compileOr(...)
     *
     * @param node Current AST node.
     * @param targetVector Target vector assigned to this subtree.
     * @param matrix MSP matrix under construction.
     */
    private void compileNode(
            PolicyNode node,
            int[] targetVector,
            MSPMatrix matrix,
            MSPContext context) {

        /*
         * Dispatch according to the concrete AST node type.
         */
        if (node instanceof AttributeNode) {

            compileAttribute(
                    (AttributeNode) node,
                    targetVector,
                    matrix,
                    context);

        }
        else if (node instanceof AndNode) {

            compileAnd(
                    (AndNode) node,
                    targetVector,
                    matrix,
                    context);

        }
        else if (node instanceof OrNode) {

            compileOr(
                    (OrNode) node,
                    targetVector,
                    matrix,
                    context);

        }
        else {

            throw new IllegalArgumentException(
                    "Unknown PolicyNode type: "
                            + node.getClass().getSimpleName());

        }

    }

    /**
     * Compiles one attribute (leaf) of the policy AST.
     *
     * According to the Monotone Span Program (MSP) construction,
     * every leaf of the access tree contributes exactly one row
     * to the matrix M.
     *
     * The target vector assigned to this subtree becomes the row
     * associated with the current attribute.
     *
     * Therefore:
     *
     *      M(row) = targetVector
     *      Ï(row) = attribute
     *
     * No vector transformation is performed at this stage.
     * Vector transformations occur only when processing
     * internal logical operators (AND / OR).
     *
     * @param node Current attribute node.
     * @param targetVector Target vector assigned to this leaf.
     * @param matrix MSP matrix under construction.
     * @param context Current compilation context.
     */
    private void compileAttribute(
            AttributeNode node,
            int[] targetVector,
            MSPMatrix matrix,
            MSPContext context) {

        /*
         * Insert one new row into the MSP matrix.
         *
         * The row vector is the target vector received
         * by this subtree.
         *
         * The corresponding attribute is recorded in
         * the mapping Ï.
         */
        matrix.addRow(
                node.getAttribute(),
                targetVector);

    }

    /**
     * Pads a target vector with trailing zeroes so that
     * its length becomes equal to the current value of c.
     *
     * This implements the first step of the Boolean
     * Formula â†’ LSSS algorithm described in
     * Appendix G of Lewko and Waters.
     *
     * If the vector already has length c,
     * it is returned unchanged.
     *
     * Example:
     *
     *      c = 4
     *
     *      (1)
     *
     * becomes
     *
     *      (1,0,0,0)
     *
     * @param vector Original target vector.
     * @param dimension Current value of c.
     * @return Zero-padded vector.
     */
    private int[] padToDimension(
            int[] vector,
            int dimension) {

        if (vector.length == dimension) {
            return vector;
        }

        int[] padded = new int[dimension];

        System.arraycopy(
                vector,
                0,
                padded,
                0,
                vector.length);

        return padded;

    }

    /**
     * Compiles an AND gate according to the Boolean
     * Formula â†’ LSSS conversion algorithm described
     * in Appendix G of Lewko and Waters.
     *
     * Let the current node be labelled by the vector v.
     *
     * The algorithm performs the following steps:
     *
     *  1. Pad v with trailing zeroes until its length
     *     becomes equal to the current value of c.
     *
     *  2. Label one child with
     *
     *          v || 1
     *
     *     where || denotes vector concatenation.
     *
     *  3. Label the other child with
     *
     *          0 || (-1)
     *
     *     where 0 is the zero vector of length c.
     *
     *  4. Increment the global counter c.
     *
     * The two child vectors satisfy
     *
     *      (v || 1) + (0 || -1)
     *          =
     *      (v || 0)
     *
     * exactly as described in Appendix G.
     *
     * @param node Current AND node.
     * @param targetVector Target vector assigned to this node.
     * @param matrix MSP matrix under construction.
     * @param context Current compilation context.
     */
    private void compileAnd(
            AndNode node,
            int[] targetVector,
            MSPMatrix matrix,
            MSPContext context) {

        /*
        * Current value of the global counter c.
        */
        int c = context.getDimension();

        /*
        * Step 1.
        *
        * Pad the parent vector so its length
        * becomes exactly c.
        */
        int[] paddedVector =
                padToDimension(targetVector, c);

        /*
        * Step 2.
        *
        * Allocate the next MSP column.
        *
        * Appendix G increments the global counter c
        * after constructing both child vectors.
        *
        * allocateColumn() performs exactly this update.
        */
        context.allocateColumn();

        /*
        * Step 3.
        *
        * Construct
        *
        *      v || 1
        */
        int[] leftVector =
                new int[c + 1];

        System.arraycopy(
                paddedVector,
                0,
                leftVector,
                0,
                c);

        leftVector[c] = 1;

        /*
        * Step 4.
        *
        * Construct
        *
        *      0 || (-1)
        */
        int[] rightVector =
                new int[c + 1];

        rightVector[c] = -1;

        /*
        * Continue recursively.
        */
        compileNode(
                node.getLeft(),
                leftVector,
                matrix,
                context);

        compileNode(
                node.getRight(),
                rightVector,
                matrix,
                context);

    }

     /**
     * Compiles an OR gate according to Appendix G
     * of Lewko and Waters.
     *
     * If an OR node is labelled by the vector v,
     * then both children inherit exactly the same
     * target vector.
     *
     * No new MSP column is introduced and the
     * global counter c remains unchanged.
     *
     * Therefore, either subtree alone is sufficient
     * to reconstruct the parent's target vector.
     *
     * @param node Current OR node.
     * @param targetVector Target vector assigned to this node.
     * @param matrix MSP matrix under construction.
     * @param context Current compilation context.
     */
        private void compileOr(
            OrNode node,
            int[] targetVector,
            MSPMatrix matrix,
            MSPContext context) {

        /*
        * Appendix G:
        *
        * Both children inherit exactly the same
        * target vector.
        */
        compileNode(
                node.getLeft(),
                targetVector,
                matrix,
                context);

        compileNode(
                node.getRight(),
                targetVector,
                matrix,
                context);

    }
        /**
         * Normalizes the MSP matrix after the complete
         * Boolean Formula â†’ LSSS conversion has finished.
         *
         * According to Appendix G of Lewko and Waters,
         * once all leaf vectors have been generated,
         * every vector must have the same dimension.
         *
         * Leaf vectors created before later AND gates may
         * therefore be shorter than the final matrix width.
         *
         * This method pads every shorter row with trailing
         * zeroes so that all rows have exactly the same
         * number of columns.
         *
         * This is the final step of the Boolean Formula
         * â†’ LSSS conversion algorithm.
         *
         * @param matrix MSP matrix under construction.
         * @param dimension Final MSP dimension (current c).
         */
        private void normalizeRows(
                MSPMatrix matrix,
                int dimension) {

        /*
        * Visit every row of the MSP matrix.
        */
        for (int i = 0; i < matrix.getRowCount(); i++) {

                int[] row = matrix.getRow(i);

                /*
                * Already normalized.
                */
                if (row.length == dimension) {
                continue;
                }

                /*
                * Create a new vector having the final
                * MSP dimension.
                */
                int[] normalizedRow = new int[dimension];

                /*
                * Copy the existing coordinates.
                * Remaining coordinates stay equal to zero.
                */
                System.arraycopy(
                        row,
                        0,
                        normalizedRow,
                        0,
                        row.length);

                /*
                * Replace the old row by the normalized one.
                */
                matrix.setRow(i, normalizedRow);

        }

        }

}