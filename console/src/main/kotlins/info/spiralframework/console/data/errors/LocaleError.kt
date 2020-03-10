package info.spiralframework.console.data.errors

import info.spiralframework.base.common.locale.SpiralLocale
import org.parboiled.Context
import org.parboiled.buffers.InputBuffer
import org.parboiled.errors.ParseError

open class LocaleError(private val _startIndex: Int, private val _endIndex: Int, private val _inputBuffer: InputBuffer, private val _errorMessage: String): ParseError {
    constructor(locale: SpiralLocale, context: Context<*>, errorMessage: String, vararg params: Any): this(context.startIndex, context.currentIndex, context.inputBuffer, locale.localiseArray(errorMessage, params))

    /**
     * Gets the start index of the parse error in the underlying input buffer.
     *
     * @return the input index of the first character covered by this error
     */
    override fun getStartIndex(): Int = _startIndex

    /**
     * Gets the inputbuffer this error occurred in.
     *
     * @return the inputbuffer
     */
    override fun getInputBuffer(): InputBuffer = _inputBuffer

    /**
     * An optional error message.
     *
     * @return an optional error message.
     */
    override fun getErrorMessage(): String = _errorMessage

    /**
     * Gets the end index of the parse error in the underlying input buffer.
     *
     * @return the end index of this error, i.e. the index of the character immediately following the last character
     * covered by this error
     */
    override fun getEndIndex(): Int = _endIndex
}