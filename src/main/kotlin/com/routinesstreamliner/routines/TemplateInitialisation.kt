package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import java.io.File
import java.io.InputStream

class TemplateInitialisation private constructor(
    val templateSource: () -> InputStream,
    val populateTemplateParams: HashMap<String, ParamValue<String>>.() -> Unit,
    val engineFactory: TemplatesEngineFactory<String>
) {

    class Builder {
        private lateinit var inputStream: () -> InputStream
        private var populateParams: HashMap<String, ParamValue<String>>.() -> Unit = {}
        private var engineFactory: TemplatesEngineFactory<String> = TemplatesEngineFactory.mustacheFactory()

        fun templateSource(inputStream: () -> InputStream) {
            this.inputStream = inputStream
        }

        fun templateSourceText(text: () -> String) {
            this.inputStream = {
                text().toByteArray().inputStream()
            }
        }

        fun templateSourceFile(filePath: () -> String) {
            this.inputStream = {
                File(filePath()).inputStream()
            }
        }

        fun populateTemplateParams(p: HashMap<String, ParamValue<String>>.() -> Unit) {
            this.populateParams = p
        }

        fun build() = TemplateInitialisation(
            templateSource = inputStream,
            populateTemplateParams = populateParams,
            engineFactory = engineFactory
        )
    }
}