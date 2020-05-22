package com.routinesstreamliner

import java.io.File
import java.io.InputStream

class NewFileFromTemplateRoutine : Routine() {

    private var templatePath = ""
    private lateinit var savePath: ParamValue<String>
    private var templateEngineFactory: TemplatesEngineFactory<String> = TemplatesEngineFactory.mustacheFactory()
    private var templateParams: HashMap<String, ParamValue<String>>.() -> Unit = { }

    override var _friendlyName: () -> String = {
        "NewFileFromTemplateRoutine | Template = $templatePath"
    }

    fun fromTemplate(path: String) {
        this.templatePath = path
    }

    fun saveTo(path: ParamValue<String>) {
        savePath = path
    }

    fun useTemplatesEngine(factoryFunction: (templateSource: InputStream) -> TemplatesEngine<String>) {
        templateEngineFactory = TemplatesEngineFactory.custom(factoryFunction)
    }

    fun templateParams(block: HashMap<String, ParamValue<String>>.() -> Unit) {
        this.templateParams = block
    }


    override fun execute() {
        File(templatePath).inputStream().use { rawTemplate ->
            val params = hashMapOf<String, ParamValue<String>>()
            templateParams(params)

            val newFile = File(savePath.get())
            newFile.parentFile.mkdirs()

            val mappedParams = params.mapValues { it.value.get() }

            templateEngineFactory.create(rawTemplate).execute(mappedParams).use { result ->
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

fun Routines.newFileFromTemplate(block: NewFileFromTemplateRoutine.() -> Unit): NewFileFromTemplateRoutine {
    val routine = NewFileFromTemplateRoutine()
    routine.block()
    this.addRoutine(routine)
    return routine
}
