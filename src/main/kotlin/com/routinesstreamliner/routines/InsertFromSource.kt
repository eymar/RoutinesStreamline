package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import java.io.ByteArrayInputStream
import java.io.InputStream

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
            templateInitialisation: TemplateInitialisation.Builder.() -> Unit
        ): InsertFromSource {
            return object : InsertFromSource {
                override fun inputStream(): InputStream {
                    val ti = TemplateInitialisation.Builder().apply(templateInitialisation)
                    return ti.build().let {
                        it.engineFactory.create(it.templateSource()).execute(
                            params = HashMap<String, ParamValue<String>>()
                                .apply(it.populateTemplateParams)
                                .mapValues { it.value.get() }
                        )
                    }
                }
            }
        }
    }
}