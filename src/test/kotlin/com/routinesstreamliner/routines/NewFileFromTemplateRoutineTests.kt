package com.routinesstreamliner.routines

import com.routinesstreamliner.ParamValue
import com.routinesstreamliner.RoutinesBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class NewFileFromTemplateRoutineTests {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should create a new file with correct content from a template`() {
        val template = "Hello {{NAME}}"

        val templateFile = tempFolder.newFile("tmp1")
        templateFile.outputStream().use {
            it.write(template.toByteArray())
        }

        val resultFile = tempFolder.newFile("res1")

        val r = NewFileFromTemplateRoutine().apply {
            templateParams {
                this["NAME"] = ParamValue.constant("Earth!")
            }

            fromTemplate(path = templateFile.absolutePath)

            saveTo(path = ParamValue {
                resultFile.absolutePath
            })
        }.build()

        r.execute()

        assertEquals("Hello Earth!", resultFile.readLines()[0])
    }

    @Test
    fun `extension function should add a routine instance to all routines`() {
        val routines = RoutinesBuilder().apply {
            newFileFromTemplate {
                friendlyName { "TestRoutineName1" }
                saveTo { ParamValue { "" } }
                fromTemplate("")
            }
        }.build()

        assertEquals( "TestRoutineName1", routines.groups[0].routines[0].friendlyName)
    }

}