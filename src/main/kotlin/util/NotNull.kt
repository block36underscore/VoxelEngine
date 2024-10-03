package gay.block36.voxel.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <T> T.notNull(): Boolean {
    contract {
        returns(true) implies (this@notNull != null)
    }
    return this != null
}