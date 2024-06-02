package io.github.andreypfau.raptorq.math

public class InactivationDecoding(
    private val l: SparseMatrixGF2,
    private val pi: Int
) {
    private val lRows = l.transpose()
    private val cols = l.cols - pi
    private val rows = l.rows
    private val colCnt = IntArray(cols)
    private val rowCnt = IntArray(rows)
    private val rowXor = IntArray(rows)
    private val rowCntOffset = IntArray(cols + 2)
    private val sortedRows = IntArray(rows)
    private val rowsPos = IntArray(rows)
    private val wasCol = BooleanArray(cols)
    private val wasRow = BooleanArray(rows)
    private val pCols = ArrayList<Int>()
    private val pRows = ArrayList<Int>()
    private val inactiveCols = ArrayList<Int>()

    public operator fun invoke(): InactivationDecodingResult {
        init()
        loop()

        for (row in 0 until rows) {
            if (!wasRow[row]) {
                pRows.add(row)
            }
        }
        val side = pCols.size
        for (col in inactiveCols.asReversed()) {
            pCols.add(col)
        }
        for (i in 0 until pi) {
            pCols.add(cols + i)
        }

        return InactivationDecodingResult(side, pRows, pCols)
    }

    private fun init() {
        l.generate { row, col ->
            if (col >= cols) {
                return@generate
            }
            colCnt[col]++
            rowCnt[row]++
            rowXor[row] = rowXor[row] xor col
        }
        sortRows()
    }

    private fun sortRows() {
        for (i in 0 until rows) {
            rowCntOffset[rowCnt[i] + 1]++
        }
        for (i in 1 until cols + 1) {
            rowCntOffset[i] += rowCntOffset[i - 1]
        }
        for (i in 0 until rows) {
            val pos = rowCntOffset[rowCnt[i]]++
            sortedRows[pos] = i
            rowsPos[i] = pos
        }
    }

    private fun loop() {
        while (rowCntOffset[1] != rows) {
            val row = sortedRows[rowCntOffset[1]]
            val col = chooseCol(row)
            val cnt = rowCnt[row]
            pCols.add(col)
            pRows.add(row)

            if (cnt == 1) {
                inactivateCol(col)
            } else {
                for (x in lRows.col(row)) {
                    if (x >= cols || wasCol[x]) {
                        continue
                    }
                    if (x != col) {
                        inactiveCols.add(x)
                    }
                    inactivateCol(x)
                }
            }
            wasRow[row] = true
        }
    }

    private fun chooseCol(row: Int): Int {
        val cnt = rowCnt[row]
        if (cnt == 0) {
            return rowXor[row]
        }
        var bestCol = -1
        for (col in l.col(row)) {
            if (col >= cols || wasCol[col]) {
                continue
            }
            if (bestCol == -1 || colCnt[col] < colCnt[bestCol]) {
                bestCol = col
            }
        }
        return bestCol
    }

    private fun inactivateCol(col: Int) {
        wasCol[col] = true
        for (row in l.col(col)) {
            if (wasRow[row]) {
                continue
            }
            val pos = rowsPos[row]
            val cnt = rowCnt[row]
            val offset = rowCntOffset[cnt]

            val tmp = sortedRows[pos]
            sortedRows[pos] = sortedRows[offset]
            sortedRows[offset] = tmp

            rowsPos[sortedRows[pos]] = pos
            rowsPos[sortedRows[offset]] = offset
            rowCntOffset[cnt]++
            rowCnt[row]--
            rowXor[row] = rowXor[row] xor col
        }
    }

    public class InactivationDecodingResult(
        public val size: Int,
        public val pRows: MutableList<Int>,
        public val pCols: MutableList<Int>
    )
}
