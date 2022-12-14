@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import com.github.andreypfau.raptorq.utils.hex

// Constants described in 5.7.3
val OCTET_EXP = hex(
    "01020408102040801D3A74E8CD8713264C982D5AB475EAC98F03060C183060C09D274E9C254A94356AD4B577EEC19F23468C050A142850A05DBA69D2B96FDEA15FBE61C2992F5EBC65CA890F1E3C78F0FDE7D3BB6BD6B17FFEE1DFA35BB671E2D9AF4386112244880D1A3468D0BD67CE811F3E7CF8EDC7933B76ECC5973366CC85172E5CB86DDAA94F9E214284152A54A84D9A2952A455AA49923972E4D5B773E6D1BF63C6913F7EFCE5D7B37BF6F1FFE3DBAB4B963162C495376EDCA557AE4182193264C88D070E1C3870E0DDA753A651A259B279F2F9EFC39B2B56AC458A09122448903D7AF4F5F7F3FBEBCB8B0B162C58B07DFAE9CF831B366CD8AD478E01020408102040801D3A74E8CD8713264C982D5AB475EAC98F03060C183060C09D274E9C254A94356AD4B577EEC19F23468C050A142850A05DBA69D2B96FDEA15FBE61C2992F5EBC65CA890F1E3C78F0FDE7D3BB6BD6B17FFEE1DFA35BB671E2D9AF4386112244880D1A3468D0BD67CE811F3E7CF8EDC7933B76ECC5973366CC85172E5CB86DDAA94F9E214284152A54A84D9A2952A455AA49923972E4D5B773E6D1BF63C6913F7EFCE5D7B37BF6F1FFE3DBAB4B963162C495376EDCA557AE4182193264C88D070E1C3870E0DDA753A651A259B279F2F9EFC39B2B56AC458A09122448903D7AF4F5F7F3FBEBCB8B0B162C58B07DFAE9CF831B366CD8AD478E"
)

// Constants described in 5.7.4
val OCTET_LOG = hex(
    "0000011902321AC603DF33EE1B68C74B0464E00E348DEF811CC169F8C8084C71058A652FE1240F2135938EDAF01282451DB5C27D6A27F9B9C99A09784DE472A606BF8B6266DD30FDE29825B31091228836D094CE8F96DBBDF1D2135C833846401E42B6A3C3487E6E6B3A2854FA85BA3DCA5E9B9F0A15792B4ED4E5AC73F3A7570770C0F78C80630D674ADEED31C5FE18E3A5997726B8B47C114492D92320892E373FD15B95BCCFCD908797B2DCFCBE61F256D3AB142A5D9E843C3953476D41A21F2D43D8B77BA476C41749EC7F0C6FF66CA13B52299D55AAFB6086B1BBCC3E5ACB595FB09CA9A0510BF516EB7A752CD74FAED5E9E6E7ADE874D6F4EAA85058AF"
)

val OCTET_MUL = calculateOctetMulTable()

private inline fun constMul(x: Int, y: Int): Byte =
    OCTET_EXP[OCTET_LOG[x].toUByte().toInt() + OCTET_LOG[y].toUByte().toInt()]

private fun calculateOctetMulTable(): Array<ByteArray> {
    val result = Array(256) { ByteArray(256) }
    var i = 1
    while (i < 256) {
        var j = 1
        while (j < 256) {
            result[i][j] = constMul(i, j)
            j++
        }
        i++
    }
    return result
}
