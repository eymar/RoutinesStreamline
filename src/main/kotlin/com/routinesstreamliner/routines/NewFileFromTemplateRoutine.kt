package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import com.routinesstreamliner.Routine
import com.routinesstreamliner.RoutinesBuilder
import java.io.File
import java.io.InputStream

class NewFileFromTemplateRoutine : Routine.Builder<Unit>() {

    private var templatePath = ""
    private lateinit var savePath: ParamValue<String>
    private var templateEngineFactory: TemplatesEngineFactory<String> =
        TemplatesEngineFactory.mustacheFactory()
    private var templateParams: HashMap<String, ParamValue<String>>.() -> Unit = { }

    fun fromTemplate(path: String) {
        this.templatePath = path
    }

    fun saveTo(path: ParamValue<String>) {
        savePath = path
    }

    fun useTemplatesEngine(factoryFunction: (templateSource: InputStream) -> TemplatesEngine<String>) {
        templateEngineFactory =
                TemplatesEngineFactory.custom(factoryFunction)
    }

    fun templateParams(block: HashMap<String, ParamValue<String>>.() -> Unit) {
        this.templateParams = block
    }

    override fun build(): Routine<Unit> {
        val templatePath = templatePath
        val savePath = savePath
        val templateParams = templateParams
        val templateEngineFactory = templateEngineFactory

        if (friendlyName == null) {
            friendlyName = this.javaClass.simpleName + " | Template: " + templatePath
        }

        routineBody {
            execute(
                templatePath = templatePath,
                savePath = savePath,
                templateParams = templateParams,
                templateEngineFactory = templateEngineFactory

            )
        }
        return super.build()
    }

    private fun execute(
        templatePath: String,
        savePath: ParamValue<String>,
        templateParams: HashMap<String, ParamValue<String>>.() -> Unit,
        templateEngineFactory: TemplatesEngineFactory<String>
    ) {
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


}

fun RoutinesBuilder.newFileFromTemplate(block: NewFileFromTemplateRoutine.() -> Unit): Routine<Unit> {
    val routine = NewFileFromTemplateRoutine().apply(block).build()
    this.addRoutine(routine)
    return routine
}
