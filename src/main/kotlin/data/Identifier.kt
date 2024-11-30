package gay.block36.voxel.data

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Identifier (val id: String) {
    val namespace: String
        get() = this.id.split(":").first()
    val name: String
        get() = this.id.takeLastWhile { it != ':' }
}