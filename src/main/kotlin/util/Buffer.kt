package gay.block36.voxel.util

import org.lwjgl.PointerBuffer

fun PointerBuffer.iter() = PointerBufferIterator(this)
fun PointerBuffer.iterable() = PointerBufferIterable(PointerBufferIterator(this))

class PointerBufferIterator(val buffer: PointerBuffer, var index: Int = 0): Iterator<Long> {
    override fun hasNext() = buffer.capacity() > index

    override fun next() = buffer[++index]
}

@JvmInline
value class PointerBufferIterable(val iterator: PointerBufferIterator) : Iterable<Long> {
    override fun iterator() = iterator
}