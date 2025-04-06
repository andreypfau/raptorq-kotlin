package io.github.andreypfau.raptorq.math

internal fun gaussianElimination(
    a: MatrixGF256,
    d: MatrixGF256,
): MatrixGF256? {
    val cols = a.cols
    val rows = d.rows
    val rowPerm = IntArray(rows) { it }

    for (row in 0 until cols) {
        var nonZeroRow = row
        while (nonZeroRow < rows && a[rowPerm[nonZeroRow], row] == Octet.ZERO) {
            nonZeroRow++
        }
        if (nonZeroRow == rows) {
            return null
        }
        if (nonZeroRow != row) {
            val tmp = rowPerm[row]
            rowPerm[row] = rowPerm[nonZeroRow]
            rowPerm[nonZeroRow] = tmp
        }
        val mul = a[rowPerm[row], row].inv()
        a.mulAssignRow(rowPerm[row], mul)
        d.mulAssignRow(rowPerm[row], mul)
        for (zeroRow in 0 until rows) {
            if (zeroRow == row) continue
            val x = a[rowPerm[zeroRow], row]
            if (x != Octet.ZERO) {
                a.addMulAssignRow(rowPerm[zeroRow], rowPerm[row], x)
                d.addMulAssignRow(rowPerm[zeroRow], rowPerm[row], x)
            }
        }
    }

    return d.applyRowPermutation(rowPerm)
}
