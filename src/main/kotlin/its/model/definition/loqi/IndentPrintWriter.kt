package its.model.definition.loqi

import java.io.PrintWriter
import java.io.Writer

/**
 * Writer, поддерживающий печать с табуляцией
 * Взято со stackoverflow
 */
class IndentPrintWriter(
    pOut: Writer,
    private val indent: String = "\t",
) : PrintWriter(pOut) {
    private var newLine = false
    private var currentIndent = ""

    /**
     * Увеличить табуляцию
     */
    fun indent() {
        currentIndent += indent
    }

    /**
     * Уменьшить табуляцию
     */
    fun unindent() {
        if (currentIndent.isEmpty()) return
        currentIndent = currentIndent.substring(0, currentIndent.length - indent.length)
    }

    override fun print(x: Any?) {
        // indent when printing at the start of a new line
        var string = x.toString()
        if (newLine) {
            super.print(currentIndent)
            newLine = false
        }

        // strip the last new line symbol (if there is one)
        val endsWithNewLine = string.endsWith("\n")
        if (endsWithNewLine) string = string.substring(0, string.length - 1)

        // print the text (add indent after new-lines)
        string = string.replace("\n".toRegex(), "\n$currentIndent")
        super.print(string)

        // finally add the stripped new-line symbol.
        if (endsWithNewLine) println()
    }

    override fun println(x: Any?) {
        print((x.toString() + "\n") as Any)
    }

    override fun println() {
        super.println()
        newLine = true
    }
}