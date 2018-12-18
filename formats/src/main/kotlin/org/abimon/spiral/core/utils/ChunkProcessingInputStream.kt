package org.abimon.spiral.core.utils

import java.io.InputStream
import java.util.*

class ChunkProcessingInputStream(val readChunk: () -> ByteArray) : InputStream() {
    private val buffer: Deque<Byte> = LinkedList()

    private fun ensureSpace(space: Int) {
        while (buffer.size < space) {
            val chunk = readChunk()
            if (chunk.isEmpty())
                break

            chunk.forEach(buffer::addLast)
        }
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an `int` in the range `0` to
     * `255`. If no byte is available because the end of the stream
     * has been reached, the value `-1` is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     *
     *  A subclass must provide an implementation of this method.
     *
     * @return     the next byte of data, or `-1` if the end of the
     * stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    override fun read(): Int {
        ensureSpace(1)

        if (buffer.isEmpty())
            return -1

        return buffer.poll().toInt() and 0xFF
    }

    /**
     * Reads up to `len` bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * `len` bytes, but a smaller number may be read.
     * The number of bytes actually read is returned as an integer.
     *
     *
     *  This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     *
     *  If `len` is zero, then no bytes are read and
     * `0` is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value `-1` is returned; otherwise, at least one
     * byte is read and stored into `b`.
     *
     *
     *  The first byte read is stored into element `b[off]`, the
     * next one into `b[off+1]`, and so on. The number of bytes read
     * is, at most, equal to `len`. Let *k* be the number of
     * bytes actually read; these bytes will be stored in elements
     * `b[off]` through `b[off+`*k*`-1]`,
     * leaving elements `b[off+`*k*`]` through
     * `b[off+len-1]` unaffected.
     *
     *
     *  In every case, elements `b[0]` through
     * `b[off]` and elements `b[off+len]` through
     * `b[b.length-1]` are unaffected.
     *
     *
     *  The `read(b,` `off,` `len)` method
     * for class `InputStream` simply calls the method
     * `read()` repeatedly. If the first such call results in an
     * `IOException`, that exception is returned from the call to
     * the `read(b,` `off,` `len)` method.  If
     * any subsequent call to `read()` results in a
     * `IOException`, the exception is caught and treated as if it
     * were end of file; the bytes read up to that point are stored into
     * `b` and the number of bytes read before the exception
     * occurred is returned. The default implementation of this method blocks
     * until the requested amount of input data `len` has been read,
     * end of file is detected, or an exception is thrown. Subclasses are encouraged
     * to provide a more efficient implementation of this method.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array `b`
     * at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     * `-1` if there is no more data because the end of
     * the stream has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the input stream has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If `b` is `null`.
     * @exception  IndexOutOfBoundsException If `off` is negative,
     * `len` is negative, or `len` is greater than
     * `b.length - off`
     * @see java.io.InputStream.read
     */
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }

        ensureSpace(len)

        if (buffer.isEmpty())
            return -1

        for (i in b.indices)
            b[off + i] = buffer.poll() ?: return i

        return len
    }

    /**
     * Skips over and discards `n` bytes of data from this input
     * stream. The `skip` method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly `0`.
     * This may result from any of a number of conditions; reaching end of file
     * before `n` bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned. If `n` is
     * negative, the `skip` method for class `InputStream` always
     * returns 0, and no bytes are skipped. Subclasses may handle the negative
     * value differently.
     *
     *
     *  The `skip` method of this class creates a
     * byte array and then repeatedly reads into it until `n` bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     * For instance, the implementation may depend on the ability to seek.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if the stream does not support seek,
     * or if some other I/O error occurs.
     */
    override fun skip(n: Long): Long {
        ensureSpace(n.toInt())

        for (i in 0 until n)
            buffer.poll() ?: return i

        return n
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     *
     *  Note that while some implementations of `InputStream` will return
     * the total number of bytes in the stream, many will not.  It is
     * never correct to use the return value of this method to allocate
     * a buffer intended to hold all data in this stream.
     *
     *
     *  A subclass' implementation of this method may choose to throw an
     * [IOException] if this input stream has been closed by
     * invoking the [.close] method.
     *
     *
     *  The `available` method for class `InputStream` always
     * returns `0`.
     *
     *
     *  This method should be overridden by subclasses.
     *
     * @return     an estimate of the number of bytes that can be read (or skipped
     * over) from this input stream without blocking or `0` when
     * it reaches the end of the input stream.
     * @exception  IOException if an I/O error occurs.
     */
    override fun available(): Int {
        return buffer.size
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     *
     *  The `close` method of `InputStream` does
     * nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    override fun close() {
        buffer.clear()
    }
}