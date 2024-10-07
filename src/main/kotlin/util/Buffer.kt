package gay.block36.voxel.util

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

fun PointerBuffer.iter() = PointerBufferIterator(this)
fun PointerBuffer.iterable() = PointerBufferIterable(PointerBufferIterator(this))

class PointerBufferIterator(val buffer: PointerBuffer, var index: Int = 0): Iterator<Long> {
    override fun hasNext() = buffer.capacity() - 1 > index

    override fun next() = buffer[++index]
}

@JvmInline
value class PointerBufferIterable(val iterator: PointerBufferIterator) : Iterable<Long> {
    override fun iterator() = iterator
}

fun asPointerBuffer(stack: MemoryStack, collection: Collection<String>): PointerBuffer {
    return stack.mallocPointer(collection.size)
        .apply layers@ {
            collection
                .mapNotNull(stack::UTF8Safe)
                .forEach(this@layers::put)
        }
        .rewind()
}

class IntBufferIterator(val buffer: IntBuffer, var index: Int = 0): Iterator<Int> {
    override fun hasNext() = buffer.capacity() - 1 > index

    override fun next() = buffer[++index]
}

fun IntBuffer.iter() = IntBufferIterator(this)