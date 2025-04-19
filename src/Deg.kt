package io.github.andreypfau.raptorq

import kotlin.math.min

public object Deg {
    public fun deg(v: Int, w: Int): Int {
        val degreeDistribution = intArrayOf(
            0, 5243, 529531, 704294, 791675, 844104, 879057, 904023, 922747, 937311, 948962,
            958494, 966438, 973160, 978921, 983914, 988283, 992138, 995565, 998631, 1001391, 1003887,
            1006157, 1008229, 1010129, 1011876, 1013490, 1014983, 1016370, 1017662, 1048576
        )
        for (i in degreeDistribution.indices) {
            if (v < degreeDistribution[i]) {
                return min(w - 2, i)
            }
        }
        throw IllegalArgumentException("Inconsistent table state")
    }
}
