package com.routinesstreamliner.routines

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

interface TemplatesEngine<T> {
    fun execute(params: Map<String, T>): InputStream


    companion object {
        private val mustacheFactory: MustacheFactory by lazy {
            DefaultMustacheFactory()
        }

        fun <T>createMustacheEngine(templateSource: InputStream): TemplatesEngine<T> {
            return object : TemplatesEngine<T> {

                private val inputReader = templateSource.reader()
                private val mustache = mustacheFactory.compile(inputReader, "TemplatesEngine - mustache")

                override fun execute(params: Map<String, T>): InputStream {
                    val baos = ByteArrayOutputStream()
                    baos.writer().use {
                        mustache.execute(it, params)
                        inputReader.close()
                    }
                    return ByteArrayInputStream(baos.toByteArray())
                }
            }
        }
    }
}

interface TemplatesEngineFactory<T> {
    fun create(source: InputStream): TemplatesEngine<T>

    companion object {
        fun <T> mustacheFactory(): TemplatesEngineFactory<T> = object :
            TemplatesEngineFactory<T> {
            override fun create(source: InputStream) =
                TemplatesEngine.createMustacheEngine<T>(source)
        }

        fun <T> custom(factoryFunction: (InputStream) -> TemplatesEngine<T>): TemplatesEngineFactory<T> {
            return object : TemplatesEngineFactory<T> {
                override fun create(source: InputStream): TemplatesEngine<T> {
                    return factoryFunction(source)
                }
            }
        }
    }
}