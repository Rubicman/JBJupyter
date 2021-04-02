import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.Charset

/*
old.txt
new.txt

 */


fun main(args: Array<String>) {
    val old = Info()
    val new = Info()
    print("old file: ")
    old.file = readLine()!!
    print("new file: ")
    new.file = readLine()!!
    old.lines = try {
        File(old.file).useLines(CHARSET) { it.toList() }
    } catch (e: IOException) {
        println("Could not read old file")
        return
    }
    new.lines = try {
        File(new.file).useLines(CHARSET) { it.toList() }
    } catch (e: IOException) {
        println("Could not read new file")
        return
    }

    old.size = old.lines.map { it.length }.sum()
    var curArray = Array(old.size + 1) { SequenceNode(0, 0, false, null) }
    var prevArray: Array<SequenceNode>
    for (newLine in new.lines.indices) {
        for (a in new.lines[newLine]) {
            var i = 0
            prevArray = curArray
            curArray = Array(old.size + 1) { SequenceNode(0, 0, false, null) }
            for (oldLine in old.lines.indices) {
                for (b in old.lines[oldLine]) {
                    i++
                    if (a == b) {
                        curArray[i] = prevArray[i - 1].next(oldLine, newLine)
                    }
                    if (curArray[i] < prevArray[i]) {
                        curArray[i] = prevArray[i].noContinue()
                    }
                    if (curArray[i] < curArray[i - 1]) {
                        curArray[i] = curArray[i - 1].noContinue()
                    }
                }
            }
        }
    }

    old.sameSizes = Array(old.lines.size) { 0 }
    new.sameSizes = Array(new.lines.size) { 0 }
    var head: AnswerNode? = curArray[curArray.size - 1].node
    while (head != null) {
        old.sameSizes[head.oldLine]++
        new.sameSizes[head.newLine]++
        head = head.prev
    }

    try {
        PrintStream(File("diff.html").outputStream(), false, CHARSET).use {
            it.appendHTML().html {
                head {
                    meta {
                        charset = CHARSET.name()
                    }
                    style {
                        unsafe {
                            raw(
                                """
                                .default {
                                    background-color: #fff;
                                }
                                .added {
                                    background-color: #67c977;
                                }
                                .deleted {
                                    background-color: #c9c9c9;
                                }
                                .changed {
                                    background-color: #695cbf;
                                }
                                p {
                                    width: fit-content;
                                    margin: 0;
                                }
                                div {
                                    display: inline-block;
                                    vertical-align: top;
                                    margin: 0 10px;
                                }
                                body {
                                    text-align: center;
                                }
                            """.trimIndent()
                            )
                        }
                    }
                }
                body {
                    div {
                        old.lines.indices.forEach { i ->
                            p {
                                classes = when (old.sameSizes[i]) {
                                    old.lines[i].length -> setOf("default")
                                    0 -> setOf("deleted")
                                    else -> setOf("changed")
                                }
                                +old.lines[i]
                            }
                        }
                    }
                    div {
                        new.lines.indices.forEach { i ->
                            p {
                                classes = when (new.sameSizes[i]) {
                                    new.lines[i].length -> setOf("default")
                                    0 -> setOf("added")
                                    else -> setOf("changed")
                                }
                                +new.lines[i]
                            }
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        print("Could not write in diff.html")
        return
    }
}

private class Info {
    var file = ""
    var lines = List(0) { "" }
    var size = 0
    var sameSizes = Array(0) { 0 }
}

private data class SequenceNode(
    val max: Int,
    val sequence: Int,
    val isContinue: Boolean,
    val node: AnswerNode?
) {
    fun noContinue() = SequenceNode(max, sequence, false, node)

    fun next(oldLine: Int, newLine: Int) =
        if (isContinue) {
            SequenceNode(max + 1, sequence + 1, true, AnswerNode(node, oldLine, newLine))
        } else {
            SequenceNode(max + 1, 1, true, AnswerNode(node, oldLine, newLine))
        }

    operator fun compareTo(other: SequenceNode): Int {
        if (max == other.max) {
            return sequence - other.sequence
        }
        return max - other.max
    }
}

private data class AnswerNode(
    val prev: AnswerNode?,
    val oldLine: Int,
    val newLine: Int,
)

private val CHARSET = Charset.defaultCharset()