package com.routinesstreamliner

import java.io.File
import java.io.InputStream

class NewFileFromTemplateRoutine(
    parentParams: Map<String, ParamValue> = emptyMap()
) : Routine(parentParams) {

    private var templatePath = ""
    private var savePath: String = ""
    private var templateEngineFactory: TemplatesEngineFactory<String> = TemplatesEngineFactory.mustacheFactory()
    private var templateParams: HashMap<String, String>.() -> Unit = { }

    fun fromTemplate(path: String) {
        this.templatePath = path
    }

    fun saveTo(path: ParamValue) {
        savePath = path.get()
    }

    fun useTemplatesEngine(factoryFunction: (templateSource: InputStream) -> TemplatesEngine<String>) {
        templateEngineFactory = TemplatesEngineFactory.custom(factoryFunction)
    }

    fun templateParams(block: HashMap<String, String>.() -> Unit) {
        this.templateParams = block
    }

    override fun execute() {
        File(templatePath).inputStream().use { rawTemplate ->
            val params = hashMapOf<String, String>()
            templateParams(params)

            val newFile = File(savePath)
            newFile.parentFile.mkdirs()

            templateEngineFactory.create(rawTemplate).execute(params).use { result ->
                newFile.outputStream().use { fos ->
                    result.copyTo(fos)
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
    this.addRoutine5(routine)
}
