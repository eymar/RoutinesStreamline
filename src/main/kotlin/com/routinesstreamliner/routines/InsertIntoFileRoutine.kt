package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import com.routinesstreamliner.Routine
import com.routinesstreamliner.RoutinesBuilder
import java.io.*

class InsertIntoFileRoutine : Routine.Builder<Unit>() {

    private lateinit var insertFromSource: () -> InsertFromSource

    private var appendComment: String? = null

    private lateinit var insertionTarget: InsertionTarget

    fun insertFrom(source: () -> InsertFromSource) {
        this.insertFromSource = source
    }

    fun insertionTarget(target: InsertionTarget) {
        this.insertionTarget = target
    }

    fun appendFile(path: String) {
        insertionTarget(target = InsertionTarget.appendFile(path))
    }

    fun prependFile(path: String) {
        insertionTarget(target = InsertionTarget.prependFile(path))
    }

    fun insertionComment(comment: String?) {
        this.appendComment = comment
    }

    override fun build(): Routine<Unit> {
        val insertFromSource = insertFromSource
        val insertionTarget = insertionTarget
        val appendComment = appendComment

        if (friendlyName == null) {
            friendlyName = this.javaClass.simpleName
        }

        routineBody = {
            execute(
                insertFromSource = insertFromSource,
                insertionTarget = insertionTarget,
                appendComment = appendComment
            )
        }
        return super.build()
    }

    private fun execute(
        insertFromSource: () -> InsertFromSource,
        insertionTarget: InsertionTarget,
        appendComment: String?
    ) {
        insertFromSource().inputStream().use { inputStream ->
            insertionTarget.useOutputStream { outputStream ->
                appendComment.takeIf { it != null }?.also {
                    outputStream.write("$it\n".toByteArray())
                }
                inputStream.copyTo(outputStream)
            }
        }
    }
}

interface InsertionTarget {
    fun useOutputStream(useOsBlock: (OutputStream) -> Unit)

    companion object {
        fun appendFile(filePath: String): InsertionTarget = object : InsertionTarget {
            private val appendToFilePath: String = filePath

            override fun useOutputStream(useOsBlock: (OutputStream) -> Unit) {
                FileOutputStream(appendToFilePath, true).use { fos ->
                    fos.write("\n".toByteArray())
                    useOsBlock(fos)
                }
            }
        }

        fun prependFile(filePath: String): InsertionTarget = object : InsertionTarget {
            private val appendToFilePath: String = filePath

            override fun useOutputStream(useOsBlock: (OutputStream) -> Unit) {
                val originalFile = File(appendToFilePath)
                val copyFile = File(originalFile.parentFile.absolutePath + "/copy_" + originalFile.name)
                copyFile.createNewFile()

                FileOutputStream(copyFile).use { fos ->
                    useOsBlock(fos)
                    originalFile.inputStream().use {
                        fos.write("\n".toByteArray())
                        it.copyTo(fos)
                    }
                }

                originalFile.delete()
                copyFile.renameTo(originalFile)
            }
        }
    }
}

interface InsertFromSource {
    fun inputStream(): InputStream

    companion object {
        fun sourceFromText(text: () -> String): InsertFromSource {
            return object : InsertFromSource {
                private val body: () -> String = text
                override fun inputStream() = ByteArrayInputStream(body().toByteArray())
            }
        }

        fun sourceFromTemplate(
            templateInput: InputStream,
            templateParams: () -> Map<String, ParamValue<String>>,
            templatesEngineFactory: TemplatesEngineFactory<String> = TemplatesEngineFactory.mustacheFactory()
        ): InsertFromSource {
            return object : InsertFromSource {
                private val params = templateParams
                private val templatesEngine = templatesEngineFactory.create(templateInput)

                override fun inputStream(): InputStream {
                    return templatesEngine.execute(params().mapValues { it.value.get() })
                }
            }
        }
    }
}

fun RoutinesBuilder.insertIntoFile(block: InsertIntoFileRoutine.() -> Unit): Routine<Unit> {
    val r = InsertIntoFileRoutine().apply(block).build()
    this.addRoutine(r)
    return r
}