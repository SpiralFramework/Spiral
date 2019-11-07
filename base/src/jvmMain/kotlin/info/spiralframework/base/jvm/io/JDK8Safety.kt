package info.spiralframework.base.jvm.io

import java.nio.Buffer
import java.nio.InvalidMarkException

/**
 * Sets this buffer's position.  If the mark is defined and larger than the
 * new position then it is discarded.
 *
 * This method is designed for JDK safety.
 *
 * @param  newPosition
 * The new position value; must be non-negative
 * and no larger than the current limit
 *
 * @return  This buffer
 *
 * @throws  IllegalArgumentException
 * If the preconditions on `newPosition` do not hold
 */
fun <T: Buffer> T.positionSafe(newPosition: Int): Buffer = (this as Buffer).position(newPosition)

/**
 * Sets this buffer's limit.  If the position is larger than the new limit
 * then it is set to the new limit.  If the mark is defined and larger than
 * the new limit then it is discarded.
 *
 * @param  newLimit
 * The new limit value; must be non-negative
 * and no larger than this buffer's capacity
 *
 * @return  This buffer
 *
 * @throws  IllegalArgumentException
 * If the preconditions on `newLimit` do not hold
 */
fun <T: Buffer> T.limitSafe(newLimit: Int): Buffer = (this as Buffer).limit(newLimit)

/**
 * Sets this buffer's mark at its position.
 *
 * @return  This buffer
 */
fun <T: Buffer> T.markSafe(): Buffer = (this as Buffer).mark()

/**
 * Resets this buffer's position to the previously-marked position.
 *
 *
 *  Invoking this method neither changes nor discards the mark's
 * value.
 *
 * @return  This buffer
 *
 * @throws  InvalidMarkException
 * If the mark has not been set
 */
fun <T: Buffer> T.resetSafe(): Buffer = (this as Buffer).reset()

/**
 * Clears this buffer.  The position is set to zero, the limit is set to
 * the capacity, and the mark is discarded.
 *
 *
 *  Invoke this method before using a sequence of channel-read or
 * *put* operations to fill this buffer.  For example:
 *
 * <blockquote><pre>
 * buf.clear();     // Prepare buffer for reading
 * in.read(buf);    // Read data</pre></blockquote>
 *
 *
 *  This method does not actually erase the data in the buffer, but it
 * is named as if it did because it will most often be used in situations
 * in which that might as well be the case.
 *
 * @return  This buffer
 */
fun <T: Buffer> T.clearSafe(): Buffer = (this as Buffer).clear()

/**
 * Flips this buffer.  The limit is set to the current position and then
 * the position is set to zero.  If the mark is defined then it is
 * discarded.
 *
 *
 *  After a sequence of channel-read or *put* operations, invoke
 * this method to prepare for a sequence of channel-write or relative
 * *get* operations.  For example:
 *
 * <blockquote><pre>
 * buf.put(magic);    // Prepend header
 * in.read(buf);      // Read data into rest of buffer
 * buf.flip();        // Flip buffer
 * out.write(buf);    // Write header + data to channel</pre></blockquote>
 *
 *
 *  This method is often used in conjunction with the [ ][java.nio.ByteBuffer.compact] method when transferring data from
 * one place to another.
 *
 * @return  This buffer
 */
fun <T: Buffer> T.flipSafe(): Buffer = (this as Buffer).flip()

/**
 * Rewinds this buffer.  The position is set to zero and the mark is
 * discarded.
 *
 *
 *  Invoke this method before a sequence of channel-write or *get*
 * operations, assuming that the limit has already been set
 * appropriately.  For example:
 *
 * <blockquote><pre>
 * out.write(buf);    // Write remaining data
 * buf.rewind();      // Rewind buffer
 * buf.get(array);    // Copy data into array</pre></blockquote>
 *
 * @return  This buffer
 */
fun <T: Buffer> T.rewindSafe(): Buffer = (this as Buffer).rewind()