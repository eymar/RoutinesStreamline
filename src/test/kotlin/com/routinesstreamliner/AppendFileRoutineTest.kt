package com.routinesstreamliner

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AppendFileRoutineTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should add itself to Routines list when ext fun called`() {
        val routines = Routines()
        assertTrue(routines.routines.isEmpty())

        routines.appendFile {
            appendFile(path = "")
            appendFrom(source = AppendFromSource.sourceFromText { "" })
        }

        assertTrue(routines.routines.size == 1)
        assertTrue(routines.routines[0] is AppendFileRoutine)
    }

    @Test
    fun `should add given text to the end of a file and keep the rest of file unchanged`() {
        val file = tempFolder.newFile("test1.txt")
        file.writeText(text = "Line 1\nLine 2\nLine 3")

        val appendText = "Appended text should be in the file's bottom"

        val r = AppendFileRoutine().apply {
            appendFile(path = file.absolutePath)
            appendFrom(source = AppendFromSource.sourceFromText { appendText })
        }

        r.execute()

        val result = tempFolder.newFile("result.txt")
        File(file.absolutePath).copyTo(target = result, overwrite = true)

        result.readLines().also {
            assertArrayEquals(
                arrayOf("Line 1", "Line 2", "Line 3", appendText),
                it.toTypedArray()
            )
        }
    }
}