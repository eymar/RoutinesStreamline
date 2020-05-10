package com.routinesstreamliner

import java.io.File

class NewFileFromTemplateRoutine(
    parentParams: Map<String, ParamValue> = emptyMap()
) : Routine(parentParams) {

    private var templatePath = ""
    private var savePath: String = ""

    fun fromTemplate(path: String) {
        this.templatePath = path
    }

    fun saveTo(path: ParamValue) {
        savePath = path.get()
    }

    override fun execute() {
        val templateFile = File(templatePath)

        val templateParamRegex = "\\{\\{.*}}".toRegex()

        templateFile.useLines {
            it.map {
                var str = it
                routineParams.asSequence().filter {
                    it.key.matches(templateParamRegex)
                }.forEach {
                    str = str.replace(it.key, it.value.get())
                }
                str
            }.also { lines ->
                File(savePath).apply {
                    this.parentFile.mkdirs()
                }.outputStream().use { fos ->
                    lines.forEach { line -> fos.write("$line\n".toByteArray()) }
                }
            }
        }
    }

    override fun createExecutionReport(): String {
        return """
            | - Template path: $templatePath
            | - New file path: $savePath
        """.trimMargin()
    }
}

fun Routines.newFileFromTemplate(block: NewFileFromTemplateRoutine.() -> Unit) {
    val routine = NewFileFromTemplateRoutine(this.params)
    routine.block()
    this.addRoutine(routine)
}
