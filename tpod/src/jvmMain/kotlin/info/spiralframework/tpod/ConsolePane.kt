package info.spiralframework.tpod

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.withContext
import java.util.*
import java.util.function.Consumer

class ConsolePane : BorderPane() {
    protected val textArea: TextArea = TextArea()
    protected val textField: TextField = TextField()
    protected val history: MutableList<String> = ArrayList()
    protected var historyPointer = 0
    private var onMessageReceivedHandler: Consumer<String>? = null

    override fun requestFocus() {
        super.requestFocus()
        textField.requestFocus()
    }

    fun setOnMessageReceivedHandler(onMessageReceivedHandler: Consumer<String>?) {
        this.onMessageReceivedHandler = onMessageReceivedHandler
    }

    suspend fun clear() = withContext(Dispatchers.JavaFx) { textArea.clear() }

    suspend fun print(text: String) = withContext(Dispatchers.JavaFx) { textArea.appendText(text) }

    suspend fun println(text: String) = withContext(Dispatchers.JavaFx) { textArea.appendText(text + System.lineSeparator()) }

    suspend fun println() = withContext(Dispatchers.JavaFx) { textArea.appendText(System.lineSeparator()) }

    init {
        textArea.isEditable = false
        setCenter(textArea)
        textField.addEventHandler(KeyEvent.KEY_RELEASED) { keyEvent ->
            when (keyEvent.code) {
                KeyCode.ENTER -> {
                    val text: String = textField.getText()
                    textArea.appendText(text + System.lineSeparator())
                    history.add(text)
                    historyPointer++
                    if (onMessageReceivedHandler != null) {
                        onMessageReceivedHandler!!.accept(text)
                    }
                    textField.clear()
                }
                KeyCode.UP -> {
                    if (historyPointer == 0) return@addEventHandler
                    historyPointer--

                    textField.text = history[historyPointer]
                    textField.selectAll()
                }
                KeyCode.DOWN -> {
                    if (historyPointer == history.size - 1) return@addEventHandler
                    historyPointer++
                    textField.text = history[historyPointer]
                    textField.selectAll()
                }
                else -> {
                }
            }
        }

        bottom = textField
    }
}