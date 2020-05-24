package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import com.routinesstreamliner.Routine
import com.routinesstreamliner.RoutinesBuilder
import java.io.File

class NewFileFromTemplateRoutine : Routine.Builder<Unit>() {

    private lateinit var savePath: ParamValue<String>
    private lateinit var templateInitialisation: TemplateInitialisation

    private var templateParamsInitialisation: HashMap<String, ParamValue<String>>.() -> Unit = { }
    private var templateFilePath: String? = null

    @Deprecated(message = "Immutability issue: API has been updated", replaceWith = ReplaceWith("templateBuilder(...)"))
    fun fromTemplate(path: String) {
        templateBuilder {
            templateSourceFile { path }
            populateTemplateParams {
                templateParamsInitialisation(this)
            }
        }
    }

    @Deprecated(message = "Immutability issue: API has been updated", replaceWith = ReplaceWith("templateBuilder(...)"))
    fun templateParams(block: HashMap<String, ParamValue<String>>.() -> Unit) {
        this.templateParamsInitialisation = block
    }

    fun templateBuilder(block: TemplateInitialisation.Builder.() -> Unit) {
        templateInitialisation = TemplateInitialisation.Builder().apply(block).build()
    }

    fun saveTo(path: ParamValue<String>) {
        savePath = path
    }

    fun saveTo(path: () -> ParamValue<String>) {
        savePath = path()
    }

    override fun build(): Routine<Unit> {
        if (!this::templateInitialisation.isInitialized) {
            val templateParamsInitialisation = templateParamsInitialisation
            val templateFilePath = templateFilePath

            templateBuilder {
                requireNotNull(templateFilePath)
                templateSourceFile { templateFilePath }
                populateTemplateParams(templateParamsInitialisation)
            }
        }

        val templateInitialisation = templateInitialisation

        if (friendlyName == null) {
            friendlyName = this.javaClass.simpleName
        }

        routineBody {
            execute(
                templateInitialisation = templateInitialisation,
                savePath = savePath
            )
        }
        return super.build()
    }


    companion object {
        private fun execute(
            templateInitialisation: TemplateInitialisation,
            savePath: ParamValue<String>
        ) {
            templateInitialisation.templateSource().use { rawTemplate ->
                val params = hashMapOf<String, ParamValue<String>>()
                templateInitialisation.populateTemplateParams(params)

                val newFile = File(savePath.get())
                newFile.parentFile.mkdirs()

                val mappedParams = params.mapValues { it.value.get() }

                templateInitialisation.engineFactory.create(rawTemplate).execute(mappedParams).use { result ->
                    newFile.outputStream().use { fos ->
                        result.copyTo(fos)
                    }
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
