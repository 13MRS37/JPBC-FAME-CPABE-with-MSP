package mrs.msp;

/**
 * MSPContext
 *
 * Maintains the current recursive compilation state used by
 * {@link MSPCompiler} while constructing an MSP matrix.
 *
 * This class is an implementation helper only.
 * It is not part of the mathematical definition of a
 * Monotone Span Program (MSP).
 */
public class MSPContext {

    /**
     * Current number of columns allocated for the MSP matrix.
     */
    private int dimension;

    /**
     * Creates a new compilation context.
     *
     * Initially, the MSP target vector is eâ‚ = [1],
     * so the matrix has one allocated column.
     * Lewko-Waters assign this variable as global counter c in Appendix G -> Converting from Boolean Formulas to LSSS Matrices
     */
    public MSPContext() {

        this.dimension = 1;

    }

    /**
     * Returns the current number of columns allocated for
     * the MSP matrix.
     *
     * @return Current MSP matrix dimension.
     */
    public int getDimension() {

        return dimension;

    }

    /**
     * Allocates one new column for the MSP matrix.
     *
     * The dimension is increased by one and the index of
     * the newly allocated column is returned.
     *
     * @return Zero-based index of the newly allocated column.
     */
    public int allocateColumn() {

        dimension++;
        return dimension - 1;

    }

}
