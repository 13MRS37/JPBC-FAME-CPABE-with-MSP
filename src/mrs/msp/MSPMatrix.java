package mrs.msp;

import java.util.ArrayList;
import java.util.List;

/**
 * MSPMatrix
 *
 * Represents a Monotone Span Program (MSP)
 *
 *      MÌ‚ = (M, Ï)
 *
 * where:
 *
 *      M  : the MSP matrix
 *      Ï  : the mapping from matrix rows to attributes
 *
 * Each row of the matrix corresponds to exactly one attribute,
 * as defined in the MSP literature.
 *
 * This class is purely a data structure.
 *
 * It does not perform:
 *
 *      â€¢ policy parsing
 *      â€¢ MSP compilation
 *      â€¢ satisfiability verification
 *
 * Those responsibilities belong respectively to:
 *
 *      PolicyTreeBuilder
 *      MSPCompiler
 */
public class MSPMatrix {

    /**
     * Matrix rows.
     *
     * Each int[] represents one row of the MSP matrix.
     */
    private final List<int[]> rows;

    /**
     * Attribute labels.
     *
     * labels.get(i) corresponds to rows.get(i).
     */
    private final List<String> labels;

    /**
     * Creates an empty MSP matrix.
     *
     * Initially the matrix contains:
     *
     *      â€¢ no rows
     *      â€¢ no attribute labels
     *
     * Rows will be added incrementally by the MSPCompiler
     * during the recursive compilation of the policy AST.
     */
    public MSPMatrix() {

        this.rows = new ArrayList<>();
        this.labels = new ArrayList<>();

    }

     /**
     * Adds one row to the MSP matrix.
     *
     * Every row corresponds to exactly one attribute.
     *
     * The row vector is appended to the matrix while the
     * associated attribute is stored in the same position
     * inside the label mapping.
     *
     * Therefore:
     *
     *      rows.get(i)
     *
     * corresponds to
     *
     *      labels.get(i)
     *
     * for every valid row index i.
     *
     * @param attribute Attribute associated with the row.
     * @param rowVector Row vector of the MSP matrix.
     */
    public void addRow(String attribute,
                       int[] rowVector) {

        rows.add(rowVector.clone());
        labels.add(attribute);

    }

        /**
     * Returns one row of the MSP matrix.
     *
     * Each row corresponds to exactly one attribute.
     *
     * The returned vector is the mathematical row stored
     * at the specified position of the MSP matrix.
     *
     * @param index Row index.
     * @return Row vector.
     */
    public int[] getRow(int index) {
        return rows.get(index).clone();
    }

    /* v1 IMPLEMENTATION
     * Replaces an existing row of the MSP matrix.
     *
     * This method is used during the final normalization
     * step of the Boolean Formula â†’ LSSS conversion,
     * where shorter row vectors are extended with
     * trailing zeroes.
     *
     * @param index Row index.
     * @param row New normalized row vector.
   
    public void setRow(
            int index,
            int[] row) {

        rows.set(index, row.clone());
    }
    */
    
    
        public void setRow(int index, int[] row) {

            if (row == null) {
                throw new IllegalArgumentException(
                        "Row cannot be null.");
            }

            if (index < 0 || index >= rows.size()) {
                throw new IndexOutOfBoundsException(
                        "Invalid row index: " + index);
            }

            rows.set(index, row.clone());
        }


        /**
     * Returns the attribute associated with a matrix row.
     *
     * The mapping satisfies:
     *
     *      labels.get(i) â†” rows.get(i)
     *
     * for every valid row index.
     *
     * @param index Row index.
     * @return Attribute label.
     */
    public String getLabel(int index) {

        return labels.get(index);

    }

        /**
     * Returns the number of rows currently stored
     * in the MSP matrix.
     *
     * @return Number of matrix rows.
     */
    public int getRowCount() {

        return rows.size();

    }

        /**
     * Returns the number of columns of the MSP matrix.
     *
     * Since every row of a valid MSP has the same length,
     * the number of columns is determined from the first row.
     *
     * An empty matrix has zero columns.
     *
     * @return Number of matrix columns.
     */
    public int getColumnCount() {

        if (rows.isEmpty()) {

            return 0;

        }

        return rows.get(0).length;

    }

        /**
     * Returns all matrix rows.
     *
     * The returned list preserves the row ordering
     * produced by the MSP compiler.
     *
     * Each row corresponds to the attribute stored
     * at the same index inside the label mapping.
     *
     * @return List of MSP matrix rows.
     */
    public List<int[]> getRows() {

        List<int[]> copy = new ArrayList<>(rows.size());

        for (int[] row : rows) {
            copy.add(row.clone());
        }

        return java.util.Collections.unmodifiableList(copy);

    }

    /** v1 IMPLEMENTATION
     * Returns the attribute mapping of the MSP matrix.
     *
     * The i-th label corresponds to the i-th row.
     *
     * @return Attribute mapping Ï.
     */
    public List<String> getLabels() {

        return java.util.Collections.unmodifiableList(
        new ArrayList<>(labels));

    }
        /**
     * Prints the complete MSP matrix in a human-readable format.
     *
     * The output contains:
     *
     *      â€¢ Number of rows
     *      â€¢ Number of columns
     *      â€¢ Attribute mapping (Ï)
     *      â€¢ Matrix rows (M)
     *
     * This method is intended for debugging, testing and
     * demonstrating the generated MSP during the evaluation
     * of the compiler.
     */
    public void print() {

        System.out.println();
        System.out.println("========== MSP Matrix ==========");
        System.out.println();

        System.out.println("Rows    : " + getRowCount());
        System.out.println("Columns : " + getColumnCount());

        System.out.println();
        System.out.printf("%-5s %-20s %s%n",
                "#",
                "Attribute",
                "Vector");

        System.out.println("-----------------------------------------------");

        for (int i = 0; i < rows.size(); i++) {

            System.out.printf("%-5d %-20s ",
                    i,
                    labels.get(i));

            System.out.print("[");

            int[] row = rows.get(i);

            for (int j = 0; j < row.length; j++) {

                System.out.print(row[j]);

                if (j < row.length - 1) {
                    System.out.print(", ");
                }

            }

            System.out.println("]");

        }

        System.out.println();

    }

}