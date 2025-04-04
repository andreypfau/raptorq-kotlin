package io.github.andreypfau.raptorq.rfc

import io.github.andreypfau.raptorq.generators.BlockGenerator
import io.github.andreypfau.raptorq.generators.IdentityGenerator
import io.github.andreypfau.raptorq.math.SparseMatrixGF2
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public data class EncodingRow(
    public val d: UInt,
    public val a: UInt,
    public val b: UInt,
    public val d1: UInt,
    public val a1: UInt,
    public val b1: UInt
) {
    init {
        if (a == 31879u && d == 2u) {
            print("")
        }
    }

    val size: Int get() = (d + d1).toInt()

    public companion object {
        public fun fromParameters(parameters: Parameters, x: Int): EncodingRow {
            var A = 53591u + parameters.j * 997u
            if (A % 2u == 0u) {
                A++
            }
            val bLocal = 10267u * (parameters.j + 1u)
            val y = bLocal + x.toUInt() * A
            val v = Rand.rand(y.toInt(), 0, 1 shl 20)
            val d = Deg.deg(v, parameters.w.toInt()).toUInt()
            val a = 1u + Rand.rand(y.toInt(), 1, (parameters.w - 1u).toInt()).toUInt()
            val b = Rand.rand(y.toInt(), 2, parameters.w.toInt()).toUInt()
            val d1 = if (d < 4u) {
                2u + Rand.rand(x, 3, 2).toUInt()
            } else {
                2u
            }
            val a1 = 1u + Rand.rand(x, 4, parameters.p1 - 1).toUInt()
            val b1 = Rand.rand(x, 5, parameters.p1).toUInt()
            return EncodingRow(d, a, b, d1, a1, b1)
        }
    }
}

public data class Parameters(
    public val k: Int,
    public val kPadded: Int,
    public val j: UInt,
    public val s: Int,
    public val h: Int,
    public val w: UInt,
    public val l: Int,
    public val p: Int,
    public val p1: Int,
    public val u: UInt,
    public val b: Int,
) {
    public fun getEncodingRow(x: Int): EncodingRow = EncodingRow.fromParameters(this, x)

    @OptIn(ExperimentalContracts::class)
    public fun encodingRowForEach(encodingRow: EncodingRow, block: (Int) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
        }
        var b1 = encodingRow.b1
        var b = encodingRow.b
        block(b.toInt())
        repeat((encodingRow.d - 1u).toInt()) {
            b = (b + encodingRow.a) % w
            block(b.toInt())
        }
        while (b1 >= p.toUInt()) {
            b1 = (b1 + encodingRow.a1) % p1.toUInt()
        }

        block((w + b1).toInt())
        repeat((encodingRow.d1 - 1u).toInt()) {
            b1 = (b1 + encodingRow.a1) % p1.toUInt()
            while (b1 >= p.toUInt()) {
                b1 = (b1 + encodingRow.a1) % p1.toUInt()
            }
            block((w + b1).toInt())
        }
    }

    public fun upperA(encodingRows: Array<EncodingRow>): SparseMatrixGF2 =
        SparseMatrixGF2(
            BlockGenerator(
                (s + encodingRows.size), l,
                LDPC1(s, b), IdentityGenerator(s), LDPC2(s, p), ENC(this, encodingRows)
            )
        )

    private class RawParameters(
        val kPadded: Int,
        val j: Int,
        val s: Int,
        val h: Int,
        val w: Int
    )

    public companion object {
        private val RFC_PARAMETERS = arrayOf(
            RawParameters(10, 254, 7, 10, 17),
            RawParameters(12, 630, 7, 10, 19),
            RawParameters(18, 682, 11, 10, 29),
            RawParameters(20, 293, 11, 10, 31),
            RawParameters(26, 80, 11, 10, 37),
            RawParameters(30, 566, 11, 10, 41),
            RawParameters(32, 860, 11, 10, 43),
            RawParameters(36, 267, 11, 10, 47),
            RawParameters(42, 822, 11, 10, 53),
            RawParameters(46, 506, 13, 10, 59),
            RawParameters(48, 589, 13, 10, 61),
            RawParameters(49, 87, 13, 10, 61),
            RawParameters(55, 520, 13, 10, 67),
            RawParameters(60, 159, 13, 10, 71),
            RawParameters(62, 235, 13, 10, 73),
            RawParameters(69, 157, 13, 10, 79),
            RawParameters(75, 502, 17, 10, 89),
            RawParameters(84, 334, 17, 10, 97),
            RawParameters(88, 583, 17, 10, 101),
            RawParameters(91, 66, 17, 10, 103),
            RawParameters(95, 352, 17, 10, 107),
            RawParameters(97, 365, 17, 10, 109),
            RawParameters(101, 562, 17, 10, 113),
            RawParameters(114, 5, 19, 10, 127),
            RawParameters(119, 603, 19, 10, 131),
            RawParameters(125, 721, 19, 10, 137),
            RawParameters(127, 28, 19, 10, 139),
            RawParameters(138, 660, 19, 10, 149),
            RawParameters(140, 829, 19, 10, 151),
            RawParameters(149, 900, 23, 10, 163),
            RawParameters(153, 930, 23, 10, 167),
            RawParameters(160, 814, 23, 10, 173),
            RawParameters(166, 661, 23, 10, 179),
            RawParameters(168, 693, 23, 10, 181),
            RawParameters(179, 780, 23, 10, 191),
            RawParameters(181, 605, 23, 10, 193),
            RawParameters(185, 551, 23, 10, 197),
            RawParameters(187, 777, 23, 10, 199),
            RawParameters(200, 491, 23, 10, 211),
            RawParameters(213, 396, 23, 10, 223),
            RawParameters(217, 764, 29, 10, 233),
            RawParameters(225, 843, 29, 10, 241),
            RawParameters(236, 646, 29, 10, 251),
            RawParameters(242, 557, 29, 10, 257),
            RawParameters(248, 608, 29, 10, 263),
            RawParameters(257, 265, 29, 10, 271),
            RawParameters(263, 505, 29, 10, 277),
            RawParameters(269, 722, 29, 10, 283),
            RawParameters(280, 263, 29, 10, 293),
            RawParameters(295, 999, 29, 10, 307),
            RawParameters(301, 874, 29, 10, 313),
            RawParameters(305, 160, 29, 10, 317),
            RawParameters(324, 575, 31, 10, 337),
            RawParameters(337, 210, 31, 10, 349),
            RawParameters(341, 513, 31, 10, 353),
            RawParameters(347, 503, 31, 10, 359),
            RawParameters(355, 558, 31, 10, 367),
            RawParameters(362, 932, 31, 10, 373),
            RawParameters(368, 404, 31, 10, 379),
            RawParameters(372, 520, 37, 10, 389),
            RawParameters(380, 846, 37, 10, 397),
            RawParameters(385, 485, 37, 10, 401),
            RawParameters(393, 728, 37, 10, 409),
            RawParameters(405, 554, 37, 10, 421),
            RawParameters(418, 471, 37, 10, 433),
            RawParameters(428, 641, 37, 10, 443),
            RawParameters(434, 732, 37, 10, 449),
            RawParameters(447, 193, 37, 10, 461),
            RawParameters(453, 934, 37, 10, 467),
            RawParameters(466, 864, 37, 10, 479),
            RawParameters(478, 790, 37, 10, 491),
            RawParameters(486, 912, 37, 10, 499),
            RawParameters(491, 617, 37, 10, 503),
            RawParameters(497, 587, 37, 10, 509),
            RawParameters(511, 800, 37, 10, 523),
            RawParameters(526, 923, 41, 10, 541),
            RawParameters(532, 998, 41, 10, 547),
            RawParameters(542, 92, 41, 10, 557),
            RawParameters(549, 497, 41, 10, 563),
            RawParameters(557, 559, 41, 10, 571),
            RawParameters(563, 667, 41, 10, 577),
            RawParameters(573, 912, 41, 10, 587),
            RawParameters(580, 262, 41, 10, 593),
            RawParameters(588, 152, 41, 10, 601),
            RawParameters(594, 526, 41, 10, 607),
            RawParameters(600, 268, 41, 10, 613),
            RawParameters(606, 212, 41, 10, 619),
            RawParameters(619, 45, 41, 10, 631),
            RawParameters(633, 898, 43, 10, 647),
            RawParameters(640, 527, 43, 10, 653),
            RawParameters(648, 558, 43, 10, 661),
            RawParameters(666, 460, 47, 10, 683),
            RawParameters(675, 5, 47, 10, 691),
            RawParameters(685, 895, 47, 10, 701),
            RawParameters(693, 996, 47, 10, 709),
            RawParameters(703, 282, 47, 10, 719),
            RawParameters(718, 513, 47, 10, 733),
            RawParameters(728, 865, 47, 10, 743),
            RawParameters(736, 870, 47, 10, 751),
            RawParameters(747, 239, 47, 10, 761),
            RawParameters(759, 452, 47, 10, 773),
            RawParameters(778, 862, 53, 10, 797),
            RawParameters(792, 852, 53, 10, 811),
            RawParameters(802, 643, 53, 10, 821),
            RawParameters(811, 543, 53, 10, 829),
            RawParameters(821, 447, 53, 10, 839),
            RawParameters(835, 321, 53, 10, 853),
            RawParameters(845, 287, 53, 10, 863),
            RawParameters(860, 12, 53, 10, 877),
            RawParameters(870, 251, 53, 10, 887),
            RawParameters(891, 30, 53, 10, 907),
            RawParameters(903, 621, 53, 10, 919),
            RawParameters(913, 555, 53, 10, 929),
            RawParameters(926, 127, 53, 10, 941),
            RawParameters(938, 400, 53, 10, 953),
            RawParameters(950, 91, 59, 10, 971),
            RawParameters(963, 916, 59, 10, 983),
            RawParameters(977, 935, 59, 10, 997),
            RawParameters(989, 691, 59, 10, 1009),
            RawParameters(1002, 299, 59, 10, 1021),
            RawParameters(1020, 282, 59, 10, 1039),
            RawParameters(1032, 824, 59, 10, 1051),
            RawParameters(1050, 536, 59, 11, 1069),
            RawParameters(1074, 596, 59, 11, 1093),
            RawParameters(1085, 28, 59, 11, 1103),
            RawParameters(1099, 947, 59, 11, 1117),
            RawParameters(1111, 162, 59, 11, 1129),
            RawParameters(1136, 536, 59, 11, 1153),
            RawParameters(1152, 1000, 61, 11, 1171),
            RawParameters(1169, 251, 61, 11, 1187),
            RawParameters(1183, 673, 61, 11, 1201),
            RawParameters(1205, 559, 61, 11, 1223),
            RawParameters(1220, 923, 61, 11, 1237),
            RawParameters(1236, 81, 67, 11, 1259),
            RawParameters(1255, 478, 67, 11, 1277),
            RawParameters(1269, 198, 67, 11, 1291),
            RawParameters(1285, 137, 67, 11, 1307),
            RawParameters(1306, 75, 67, 11, 1327),
            RawParameters(1347, 29, 67, 11, 1367),
            RawParameters(1361, 231, 67, 11, 1381),
            RawParameters(1389, 532, 67, 11, 1409),
            RawParameters(1404, 58, 67, 11, 1423),
            RawParameters(1420, 60, 67, 11, 1439),
            RawParameters(1436, 964, 71, 11, 1459),
            RawParameters(1461, 624, 71, 11, 1483),
            RawParameters(1477, 502, 71, 11, 1499),
            RawParameters(1502, 636, 71, 11, 1523),
            RawParameters(1522, 986, 71, 11, 1543),
            RawParameters(1539, 950, 71, 11, 1559),
            RawParameters(1561, 735, 73, 11, 1583),
            RawParameters(1579, 866, 73, 11, 1601),
            RawParameters(1600, 203, 73, 11, 1621),
            RawParameters(1616, 83, 73, 11, 1637),
            RawParameters(1649, 14, 73, 11, 1669),
            RawParameters(1673, 522, 79, 11, 1699),
            RawParameters(1698, 226, 79, 11, 1723),
            RawParameters(1716, 282, 79, 11, 1741),
            RawParameters(1734, 88, 79, 11, 1759),
            RawParameters(1759, 636, 79, 11, 1783),
            RawParameters(1777, 860, 79, 11, 1801),
            RawParameters(1800, 324, 79, 11, 1823),
            RawParameters(1824, 424, 79, 11, 1847),
            RawParameters(1844, 999, 79, 11, 1867),
            RawParameters(1863, 682, 83, 11, 1889),
            RawParameters(1887, 814, 83, 11, 1913),
            RawParameters(1906, 979, 83, 11, 1931),
            RawParameters(1926, 538, 83, 11, 1951),
            RawParameters(1954, 278, 83, 11, 1979),
            RawParameters(1979, 580, 83, 11, 2003),
            RawParameters(2005, 773, 83, 11, 2029),
            RawParameters(2040, 911, 89, 11, 2069),
            RawParameters(2070, 506, 89, 11, 2099),
            RawParameters(2103, 628, 89, 11, 2131),
            RawParameters(2125, 282, 89, 11, 2153),
            RawParameters(2152, 309, 89, 11, 2179),
            RawParameters(2195, 858, 89, 11, 2221),
            RawParameters(2217, 442, 89, 11, 2243),
            RawParameters(2247, 654, 89, 11, 2273),
            RawParameters(2278, 82, 97, 11, 2311),
            RawParameters(2315, 428, 97, 11, 2347),
            RawParameters(2339, 442, 97, 11, 2371),
            RawParameters(2367, 283, 97, 11, 2399),
            RawParameters(2392, 538, 97, 11, 2423),
            RawParameters(2416, 189, 97, 11, 2447),
            RawParameters(2447, 438, 97, 11, 2477),
            RawParameters(2473, 912, 97, 11, 2503),
            RawParameters(2502, 1, 97, 11, 2531),
            RawParameters(2528, 167, 97, 11, 2557),
            RawParameters(2565, 272, 97, 11, 2593),
            RawParameters(2601, 209, 101, 11, 2633),
            RawParameters(2640, 927, 101, 11, 2671),
            RawParameters(2668, 386, 101, 11, 2699),
            RawParameters(2701, 653, 101, 11, 2731),
            RawParameters(2737, 669, 101, 11, 2767),
            RawParameters(2772, 431, 101, 11, 2801),
            RawParameters(2802, 793, 103, 11, 2833),
            RawParameters(2831, 588, 103, 11, 2861),
            RawParameters(2875, 777, 107, 11, 2909),
            RawParameters(2906, 939, 107, 11, 2939),
            RawParameters(2938, 864, 107, 11, 2971),
            RawParameters(2979, 627, 107, 11, 3011),
            RawParameters(3015, 265, 109, 11, 3049),
            RawParameters(3056, 976, 109, 11, 3089),
            RawParameters(3101, 988, 113, 11, 3137),
            RawParameters(3151, 507, 113, 11, 3187),
            RawParameters(3186, 640, 113, 11, 3221),
            RawParameters(3224, 15, 113, 11, 3259),
            RawParameters(3265, 667, 113, 11, 3299),
            RawParameters(3299, 24, 127, 11, 3347),
            RawParameters(3344, 877, 127, 11, 3391),
            RawParameters(3387, 240, 127, 11, 3433),
            RawParameters(3423, 720, 127, 11, 3469),
            RawParameters(3466, 93, 127, 11, 3511),
            RawParameters(3502, 919, 127, 11, 3547),
            RawParameters(3539, 635, 127, 11, 3583),
            RawParameters(3579, 174, 127, 11, 3623),
            RawParameters(3616, 647, 127, 11, 3659),
            RawParameters(3658, 820, 127, 11, 3701),
            RawParameters(3697, 56, 127, 11, 3739),
            RawParameters(3751, 485, 127, 11, 3793),
            RawParameters(3792, 210, 127, 11, 3833),
            RawParameters(3840, 124, 127, 11, 3881),
            RawParameters(3883, 546, 127, 11, 3923),
            RawParameters(3924, 954, 131, 11, 3967),
            RawParameters(3970, 262, 131, 11, 4013),
            RawParameters(4015, 927, 131, 11, 4057),
            RawParameters(4069, 957, 131, 11, 4111),
            RawParameters(4112, 726, 137, 11, 4159),
            RawParameters(4165, 583, 137, 11, 4211),
            RawParameters(4207, 782, 137, 11, 4253),
            RawParameters(4252, 37, 137, 11, 4297),
            RawParameters(4318, 758, 137, 11, 4363),
            RawParameters(4365, 777, 137, 11, 4409),
            RawParameters(4418, 104, 139, 11, 4463),
            RawParameters(4468, 476, 139, 11, 4513),
            RawParameters(4513, 113, 149, 11, 4567),
            RawParameters(4567, 313, 149, 11, 4621),
            RawParameters(4626, 102, 149, 11, 4679),
            RawParameters(4681, 501, 149, 11, 4733),
            RawParameters(4731, 332, 149, 11, 4783),
            RawParameters(4780, 786, 149, 11, 4831),
            RawParameters(4838, 99, 149, 11, 4889),
            RawParameters(4901, 658, 149, 11, 4951),
            RawParameters(4954, 794, 149, 11, 5003),
            RawParameters(5008, 37, 151, 11, 5059),
            RawParameters(5063, 471, 151, 11, 5113),
            RawParameters(5116, 94, 157, 11, 5171),
            RawParameters(5172, 873, 157, 11, 5227),
            RawParameters(5225, 918, 157, 11, 5279),
            RawParameters(5279, 945, 157, 11, 5333),
            RawParameters(5334, 211, 157, 11, 5387),
            RawParameters(5391, 341, 157, 11, 5443),
            RawParameters(5449, 11, 163, 11, 5507),
            RawParameters(5506, 578, 163, 11, 5563),
            RawParameters(5566, 494, 163, 11, 5623),
            RawParameters(5637, 694, 163, 11, 5693),
            RawParameters(5694, 252, 163, 11, 5749),
            RawParameters(5763, 451, 167, 11, 5821),
            RawParameters(5823, 83, 167, 11, 5881),
            RawParameters(5896, 689, 167, 11, 5953),
            RawParameters(5975, 488, 173, 11, 6037),
            RawParameters(6039, 214, 173, 11, 6101),
            RawParameters(6102, 17, 173, 11, 6163),
            RawParameters(6169, 469, 173, 11, 6229),
            RawParameters(6233, 263, 179, 11, 6299),
            RawParameters(6296, 309, 179, 11, 6361),
            RawParameters(6363, 984, 179, 11, 6427),
            RawParameters(6427, 123, 179, 11, 6491),
            RawParameters(6518, 360, 179, 11, 6581),
            RawParameters(6589, 863, 181, 11, 6653),
            RawParameters(6655, 122, 181, 11, 6719),
            RawParameters(6730, 522, 191, 11, 6803),
            RawParameters(6799, 539, 191, 11, 6871),
            RawParameters(6878, 181, 191, 11, 6949),
            RawParameters(6956, 64, 191, 11, 7027),
            RawParameters(7033, 387, 191, 11, 7103),
            RawParameters(7108, 967, 191, 11, 7177),
            RawParameters(7185, 843, 191, 11, 7253),
            RawParameters(7281, 999, 193, 11, 7351),
            RawParameters(7360, 76, 197, 11, 7433),
            RawParameters(7445, 142, 197, 11, 7517),
            RawParameters(7520, 599, 197, 11, 7591),
            RawParameters(7596, 576, 199, 11, 7669),
            RawParameters(7675, 176, 211, 11, 7759),
            RawParameters(7770, 392, 211, 11, 7853),
            RawParameters(7855, 332, 211, 11, 7937),
            RawParameters(7935, 291, 211, 11, 8017),
            RawParameters(8030, 913, 211, 11, 8111),
            RawParameters(8111, 608, 211, 11, 8191),
            RawParameters(8194, 212, 211, 11, 8273),
            RawParameters(8290, 696, 211, 11, 8369),
            RawParameters(8377, 931, 223, 11, 8467),
            RawParameters(8474, 326, 223, 11, 8563),
            RawParameters(8559, 228, 223, 11, 8647),
            RawParameters(8654, 706, 223, 11, 8741),
            RawParameters(8744, 144, 223, 11, 8831),
            RawParameters(8837, 83, 223, 11, 8923),
            RawParameters(8928, 743, 223, 11, 9013),
            RawParameters(9019, 187, 223, 11, 9103),
            RawParameters(9111, 654, 227, 11, 9199),
            RawParameters(9206, 359, 227, 11, 9293),
            RawParameters(9303, 493, 229, 11, 9391),
            RawParameters(9400, 369, 233, 11, 9491),
            RawParameters(9497, 981, 233, 11, 9587),
            RawParameters(9601, 276, 239, 11, 9697),
            RawParameters(9708, 647, 239, 11, 9803),
            RawParameters(9813, 389, 239, 11, 9907),
            RawParameters(9916, 80, 239, 11, 10009),
            RawParameters(10017, 396, 241, 11, 10111),
            RawParameters(10120, 580, 251, 11, 10223),
            RawParameters(10241, 873, 251, 11, 10343),
            RawParameters(10351, 15, 251, 11, 10453),
            RawParameters(10458, 976, 251, 11, 10559),
            RawParameters(10567, 584, 251, 11, 10667),
            RawParameters(10676, 267, 257, 11, 10781),
            RawParameters(10787, 876, 257, 11, 10891),
            RawParameters(10899, 642, 257, 12, 11003),
            RawParameters(11015, 794, 257, 12, 11119),
            RawParameters(11130, 78, 263, 12, 11239),
            RawParameters(11245, 736, 263, 12, 11353),
            RawParameters(11358, 882, 269, 12, 11471),
            RawParameters(11475, 251, 269, 12, 11587),
            RawParameters(11590, 434, 269, 12, 11701),
            RawParameters(11711, 204, 269, 12, 11821),
            RawParameters(11829, 256, 271, 12, 11941),
            RawParameters(11956, 106, 277, 12, 12073),
            RawParameters(12087, 375, 277, 12, 12203),
            RawParameters(12208, 148, 277, 12, 12323),
            RawParameters(12333, 496, 281, 12, 12451),
            RawParameters(12460, 88, 281, 12, 12577),
            RawParameters(12593, 826, 293, 12, 12721),
            RawParameters(12726, 71, 293, 12, 12853),
            RawParameters(12857, 925, 293, 12, 12983),
            RawParameters(13002, 760, 293, 12, 13127),
            RawParameters(13143, 130, 293, 12, 13267),
            RawParameters(13284, 641, 307, 12, 13421),
            RawParameters(13417, 400, 307, 12, 13553),
            RawParameters(13558, 480, 307, 12, 13693),
            RawParameters(13695, 76, 307, 12, 13829),
            RawParameters(13833, 665, 307, 12, 13967),
            RawParameters(13974, 910, 307, 12, 14107),
            RawParameters(14115, 467, 311, 12, 14251),
            RawParameters(14272, 964, 311, 12, 14407),
            RawParameters(14415, 625, 313, 12, 14551),
            RawParameters(14560, 362, 317, 12, 14699),
            RawParameters(14713, 759, 317, 12, 14851),
            RawParameters(14862, 728, 331, 12, 15013),
            RawParameters(15011, 343, 331, 12, 15161),
            RawParameters(15170, 113, 331, 12, 15319),
            RawParameters(15325, 137, 331, 12, 15473),
            RawParameters(15496, 308, 331, 12, 15643),
            RawParameters(15651, 800, 337, 12, 15803),
            RawParameters(15808, 177, 337, 12, 15959),
            RawParameters(15977, 961, 337, 12, 16127),
            RawParameters(16161, 958, 347, 12, 16319),
            RawParameters(16336, 72, 347, 12, 16493),
            RawParameters(16505, 732, 347, 12, 16661),
            RawParameters(16674, 145, 349, 12, 16831),
            RawParameters(16851, 577, 353, 12, 17011),
            RawParameters(17024, 305, 353, 12, 17183),
            RawParameters(17195, 50, 359, 12, 17359),
            RawParameters(17376, 351, 359, 12, 17539),
            RawParameters(17559, 175, 367, 12, 17729),
            RawParameters(17742, 727, 367, 12, 17911),
            RawParameters(17929, 902, 367, 12, 18097),
            RawParameters(18116, 409, 373, 12, 18289),
            RawParameters(18309, 776, 373, 12, 18481),
            RawParameters(18503, 586, 379, 12, 18679),
            RawParameters(18694, 451, 379, 12, 18869),
            RawParameters(18909, 287, 383, 12, 19087),
            RawParameters(19126, 246, 389, 12, 19309),
            RawParameters(19325, 222, 389, 12, 19507),
            RawParameters(19539, 563, 397, 12, 19727),
            RawParameters(19740, 839, 397, 12, 19927),
            RawParameters(19939, 897, 401, 12, 20129),
            RawParameters(20152, 409, 401, 12, 20341),
            RawParameters(20355, 618, 409, 12, 20551),
            RawParameters(20564, 439, 409, 12, 20759),
            RawParameters(20778, 95, 419, 13, 20983),
            RawParameters(20988, 448, 419, 13, 21191),
            RawParameters(21199, 133, 419, 13, 21401),
            RawParameters(21412, 938, 419, 13, 21613),
            RawParameters(21629, 423, 431, 13, 21841),
            RawParameters(21852, 90, 431, 13, 22063),
            RawParameters(22073, 640, 431, 13, 22283),
            RawParameters(22301, 922, 433, 13, 22511),
            RawParameters(22536, 250, 439, 13, 22751),
            RawParameters(22779, 367, 439, 13, 22993),
            RawParameters(23010, 447, 443, 13, 23227),
            RawParameters(23252, 559, 449, 13, 23473),
            RawParameters(23491, 121, 457, 13, 23719),
            RawParameters(23730, 623, 457, 13, 23957),
            RawParameters(23971, 450, 457, 13, 24197),
            RawParameters(24215, 253, 461, 13, 24443),
            RawParameters(24476, 106, 467, 13, 24709),
            RawParameters(24721, 863, 467, 13, 24953),
            RawParameters(24976, 148, 479, 13, 25219),
            RawParameters(25230, 427, 479, 13, 25471),
            RawParameters(25493, 138, 479, 13, 25733),
            RawParameters(25756, 794, 487, 13, 26003),
            RawParameters(26022, 247, 487, 13, 26267),
            RawParameters(26291, 562, 491, 13, 26539),
            RawParameters(26566, 53, 499, 13, 26821),
            RawParameters(26838, 135, 499, 13, 27091),
            RawParameters(27111, 21, 503, 13, 27367),
            RawParameters(27392, 201, 509, 13, 27653),
            RawParameters(27682, 169, 521, 13, 27953),
            RawParameters(27959, 70, 521, 13, 28229),
            RawParameters(28248, 386, 521, 13, 28517),
            RawParameters(28548, 226, 523, 13, 28817),
            RawParameters(28845, 3, 541, 13, 29131),
            RawParameters(29138, 769, 541, 13, 29423),
            RawParameters(29434, 590, 541, 13, 29717),
            RawParameters(29731, 672, 541, 13, 30013),
            RawParameters(30037, 713, 547, 13, 30323),
            RawParameters(30346, 967, 547, 13, 30631),
            RawParameters(30654, 368, 557, 14, 30949),
            RawParameters(30974, 348, 557, 14, 31267),
            RawParameters(31285, 119, 563, 14, 31583),
            RawParameters(31605, 503, 569, 14, 31907),
            RawParameters(31948, 181, 571, 14, 32251),
            RawParameters(32272, 394, 577, 14, 32579),
            RawParameters(32601, 189, 587, 14, 32917),
            RawParameters(32932, 210, 587, 14, 33247),
            RawParameters(33282, 62, 593, 14, 33601),
            RawParameters(33623, 273, 593, 14, 33941),
            RawParameters(33961, 554, 599, 14, 34283),
            RawParameters(34302, 936, 607, 14, 34631),
            RawParameters(34654, 483, 607, 14, 34981),
            RawParameters(35031, 397, 613, 14, 35363),
            RawParameters(35395, 241, 619, 14, 35731),
            RawParameters(35750, 500, 631, 14, 36097),
            RawParameters(36112, 12, 631, 14, 36457),
            RawParameters(36479, 958, 641, 14, 36833),
            RawParameters(36849, 524, 641, 14, 37201),
            RawParameters(37227, 8, 643, 14, 37579),
            RawParameters(37606, 100, 653, 14, 37967),
            RawParameters(37992, 339, 653, 14, 38351),
            RawParameters(38385, 804, 659, 14, 38749),
            RawParameters(38787, 510, 673, 14, 39163),
            RawParameters(39176, 18, 673, 14, 39551),
            RawParameters(39576, 412, 677, 14, 39953),
            RawParameters(39980, 394, 683, 14, 40361),
            RawParameters(40398, 830, 691, 15, 40787),
            RawParameters(40816, 535, 701, 15, 41213),
            RawParameters(41226, 199, 701, 15, 41621),
            RawParameters(41641, 27, 709, 15, 42043),
            RawParameters(42067, 298, 709, 15, 42467),
            RawParameters(42490, 368, 719, 15, 42899),
            RawParameters(42916, 755, 727, 15, 43331),
            RawParameters(43388, 379, 727, 15, 43801),
            RawParameters(43840, 73, 733, 15, 44257),
            RawParameters(44279, 387, 739, 15, 44701),
            RawParameters(44729, 457, 751, 15, 45161),
            RawParameters(45183, 761, 751, 15, 45613),
            RawParameters(45638, 855, 757, 15, 46073),
            RawParameters(46104, 370, 769, 15, 46549),
            RawParameters(46574, 261, 769, 15, 47017),
            RawParameters(47047, 299, 787, 15, 47507),
            RawParameters(47523, 920, 787, 15, 47981),
            RawParameters(48007, 269, 787, 15, 48463),
            RawParameters(48489, 862, 797, 15, 48953),
            RawParameters(48976, 349, 809, 15, 49451),
            RawParameters(49470, 103, 809, 15, 49943),
            RawParameters(49978, 115, 821, 15, 50461),
            RawParameters(50511, 93, 821, 16, 50993),
            RawParameters(51017, 982, 827, 16, 51503),
            RawParameters(51530, 432, 839, 16, 52027),
            RawParameters(52062, 340, 853, 16, 52571),
            RawParameters(52586, 173, 853, 16, 53093),
            RawParameters(53114, 421, 857, 16, 53623),
            RawParameters(53650, 330, 863, 16, 54163),
            RawParameters(54188, 624, 877, 16, 54713),
            RawParameters(54735, 233, 877, 16, 55259),
            RawParameters(55289, 362, 883, 16, 55817),
            RawParameters(55843, 963, 907, 16, 56393),
            RawParameters(56403, 471, 907, 16, 56951)
        )

        public fun fromK(k: Int): Parameters {
            for (rfcParameter in RFC_PARAMETERS) {
                if (rfcParameter.kPadded >= k) {
                    return fromRawParameters(k, rfcParameter)
                }
            }
            throw IllegalArgumentException("No parameters found for k = $k")
        }

        private fun fromRawParameters(k: Int, rawParameters: RawParameters): Parameters {
            val kPadded = rawParameters.kPadded
            val j = rawParameters.j.toUInt()
            val s = rawParameters.s
            val h = rawParameters.h
            val w = rawParameters.w.toUInt()
            val l = kPadded.toUInt() + s.toUInt() + h.toUInt()
            val p = l - w
            val u = p - h.toUInt()
            val b = (w - s.toUInt()).toInt()
            var p1 = (p + 1u).toInt()
            while (!isPrime(p1)) {
                p1++
            }
            return Parameters(k, kPadded, j, s, h, w, l.toInt(), p.toInt(), p1, u, b)
        }

        private fun isPrime(n: Int): Boolean {
            if (n <= 3) {
                return true
            }
            if (n % 2 == 0 || n % 3 == 0) {
                return false
            }
            var i = 5
            var w = 2
            while (i * i <= n) {
                if (n % i == 0) {
                    return false
                }
                i += w
                w = 6 - w
            }
            return true
        }
    }
}
