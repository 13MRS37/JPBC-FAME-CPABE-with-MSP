
package mrs.msp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

/**
 * Reconstructs MSP coefficients directly in the JPBC field Zr.
 *
 * Solves:
 *
 *     M_I^T * lambda = e_1
 *
 * where M_I contains the rows whose attributes are available
 * to the user.
 */
public final class ZrMSPReconstruction {

    private final boolean satisfied;
    private final List<Integer> selectedRows;
    private final Element[] coefficients;

    private ZrMSPReconstruction(
            boolean satisfied,
            List<Integer> selectedRows,
            Element[] coefficients) {

        this.satisfied = satisfied;
        this.selectedRows =
                Collections.unmodifiableList(
                        new ArrayList<>(selectedRows));

        this.coefficients = new Element[coefficients.length];

        for (int i = 0; i < coefficients.length; i++) {
            this.coefficients[i] = coefficients[i].duplicate();
        }
    }

    public static ZrMSPReconstruction failure() {
        return new ZrMSPReconstruction(
                false,
                Collections.emptyList(),
                new Element[0]);
    }

    public static ZrMSPReconstruction reconstruct(
            MSPMatrix matrix,
            Set<String> attributes,
            Field zr) {

        if (matrix == null || attributes == null || zr == null) {
            throw new IllegalArgumentException(
                    "Matrix, attributes and Zr must not be null.");
        }

        int rowCount = matrix.getRowCount();
        int columnCount = matrix.getColumnCount();

        if (rowCount == 0 || columnCount == 0) {
            return failure();
        }

        // Select every row whose label is available to the user.
        // Repeated attribute labels intentionally remain separate rows.
        List<Integer> selected = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            if (attributes.contains(matrix.getLabel(i))) {
                selected.add(i);
            }
        }

        int variableCount = selected.size();

        if (variableCount == 0) {
            return failure();
        }

        // Augmented system:
        // M_I^T * lambda = e_1
        //
        // There are columnCount equations and variableCount unknowns.
        Element[][] a =
                new Element[columnCount][variableCount + 1];

        for (int equation = 0;
             equation < columnCount;
             equation++) {

            for (int variable = 0;
                 variable < variableCount;
                 variable++) {

                int[] row = matrix.getRow(selected.get(variable));

                a[equation][variable] =
                        zr.newElement(row[equation]);
            }

            a[equation][variableCount] =
                    equation == 0
                            ? zr.newOneElement()
                            : zr.newZeroElement();
        }

        int pivotRow = 0;
        int[] pivotForColumn = new int[variableCount];

        for (int i = 0; i < variableCount; i++) {
            pivotForColumn[i] = -1;
        }

        // Gauss-Jordan elimination over Zr.
        for (int column = 0;
             column < variableCount && pivotRow < columnCount;
             column++) {

            int found = -1;

            for (int r = pivotRow; r < columnCount; r++) {
                if (!a[r][column].isZero()) {
                    found = r;
                    break;
                }
            }

            if (found == -1) {
                continue;
            }

            Element[] temp = a[pivotRow];
            a[pivotRow] = a[found];
            a[found] = temp;

            Element inverse =
                    a[pivotRow][column].duplicate().invert();

            for (int c = column; c <= variableCount; c++) {
                a[pivotRow][c].mul(inverse);
            }

            for (int r = 0; r < columnCount; r++) {
                if (r == pivotRow || a[r][column].isZero()) {
                    continue;
                }

                Element factor = a[r][column].duplicate();

                for (int c = column; c <= variableCount; c++) {
                    Element term =
                            factor.duplicate().mul(a[pivotRow][c]);

                    a[r][c].sub(term);
                }
            }

            pivotForColumn[column] = pivotRow;
            pivotRow++;
        }

        // Reject inconsistent systems.
        for (int r = 0; r < columnCount; r++) {
            boolean allZero = true;

            for (int c = 0; c < variableCount; c++) {
                if (!a[r][c].isZero()) {
                    allZero = false;
                    break;
                }
            }

            if (allZero && !a[r][variableCount].isZero()) {
                return failure();
            }
        }

        // Set free variables to zero and read pivot variables.
        Element[] lambda = new Element[variableCount];

        for (int i = 0; i < variableCount; i++) {
            lambda[i] = zr.newZeroElement();

            if (pivotForColumn[i] != -1) {
                lambda[i] =
                        a[pivotForColumn[i]][variableCount].duplicate();
            }
        }
        // Verify the reconstruction equation over Zr.
        for (int equation = 0; equation < columnCount; equation++) {
            Element sum = zr.newZeroElement();

            for (int variable = 0; variable < variableCount; variable++) {
                int[] row = matrix.getRow(selected.get(variable));

                Element term = zr.newElement(row[equation]);
                term.mul(lambda[variable]);
                sum.add(term);
            }

            Element expected = equation == 0
                    ? zr.newOneElement()
                    : zr.newZeroElement();

            if (!sum.isEqual(expected)) {
                return failure();
            }
        }

        return new ZrMSPReconstruction(true, selected, lambda);
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public List<Integer> getSelectedRows() {
        return selectedRows;
    }

    public Element[] getCoefficients() {
        Element[] copy = new Element[coefficients.length];

        for (int i = 0; i < coefficients.length; i++) {
            copy[i] = coefficients[i].duplicate();
        }

        return copy;
    }
}