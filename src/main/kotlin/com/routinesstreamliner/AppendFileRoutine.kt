package com.routinesstreamliner

import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.io.InputStream

class AppendFileRoutine(
    parentParams: Map<String, ParamValue> = emptyMap()
) : Routine(parentParams) {

    private lateinit var appendFromSource: AppendFromSource

    private lateinit var fileToAppendPath: String

    fun appendFrom(source: AppendFromSource) {
        this.appendFromSource = source
    }

    fun appendFile(path: String) {
        this.fileToAppendPath = path
    }

    override fun execute() {
        val templateParamRegex = "\\{\\{.*}}".toRegex()

        appendFromSource.inputStream().use { inputStream ->
            inputStream.bufferedReader().useLines {
                it.map {
                    var str = it
                    routineParams.asSequence().filter {
                        it.key.matches(templateParamRegex)
                    }.forEach {
                        str = str.replace(it.key, it.value.get())
                    }
                    str
                }.also { lines ->
                    FileOutputStream(fileToAppendPath, true).use { fos ->
                        lines.forEach {
                            fos.write("\n$it".toByteArray())
                        }
                    }
                }
            }
        }

    }
}

interface AppendFromSource {
    fun inputStream(): InputStream

    companion object {
        fun sourceFromText(text: () -> String): AppendFromSource {
            return object : AppendFromSource {
                private val body: () -> String = text
                override fun inputStream() = ByteArrayInputStream(body().toByteArray())
            }
        }
    }
}

fun Routines.appendFile(block: AppendFileRoutine.() -> Unit) {
    val r = AppendFileRoutine()
    r.block()
    this.addRoutine(r)
}