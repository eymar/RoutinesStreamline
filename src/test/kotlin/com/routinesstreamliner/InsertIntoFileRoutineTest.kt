package com.routinesstreamliner

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals

class InsertIntoFileRoutineTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should add itself to Routines group when ext fun called`() {
        val routines = Routines()

        routines.insertIntoFile {
            appendFile(path = "")
            insertFrom { InsertFromSource.sourceFromText { "" } }
        }

        assertTrue(routines.routinesGroups.size == 1)
        assertTrue(routines.routinesGroups.first().routines.first() is InsertIntoFileRoutine)
    }

    @Test
    fun `should add given text to the end of a file and keep the rest of file unchanged`() {
        val file = tempFolder.newFile("test1.txt")
        file.writeText(text = "Line 1\nLine 2\nLine 3")

        val appendText = "Appended text should be in the file's bottom"

        val r = InsertIntoFileRoutine().apply {
            appendFile(path = file.absolutePath)
            insertFrom { InsertFromSource.sourceFromText { appendText } }
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

    @Test
    fun `should add given comment and text to the end of a file and keep the rest of file unchanged`() {
        val file = tempFolder.newFile("test1.txt")
        file.writeText(text = "Line 1\nLine 2\nLine 3")

        val appendText = "Appended text should be in the file's bottom"
        val comment = "// This a comment before appended text"

        val r = InsertIntoFileRoutine().apply {
            appendFile(path = file.absolutePath)
            insertFrom { InsertFromSource.sourceFromText { appendText } }
            insertionComment(comment = comment)
        }

        r.execute()

        val result = tempFolder.newFile("result.txt")
        File(file.absolutePath).copyTo(target = result, overwrite = true)

        result.readLines().also {
            assertArrayEquals(
                arrayOf("Line 1", "Line 2", "Line 3", comment, appendText),
                it.toTypedArray()
            )
        }
    }

    @Test
    fun `should add given text to the start of file and keep the rest of file unchanged`() {
        val file = tempFolder.newFile("test1.txt")
        file.writeText(text = "Line 1\nLine 2\nLine 3")

        val prependText = "Prepended text should be in the file's top"

        val r = InsertIntoFileRoutine().apply {
            prependFile(file.absolutePath)
            insertFrom { InsertFromSource.sourceFromText { prependText } }
        }

        r.execute()

        val result = tempFolder.newFile("result.txt")
        File(file.absolutePath).copyTo(target = result, overwrite = true)

        result.readLines().also {
            assertArrayEquals(
                arrayOf(prependText, "Line 1", "Line 2", "Line 3"),
                it.toTypedArray()
            )
        }
    }

    @Test
    fun `should execute template and add its text to output`() {
        val baos = ByteArrayOutputStream()
        baos.write("First line\n".toByteArray())

        val template = "Hello {{name}}!"

        val result = InsertFromSource.sourceFromTemplate(
            templateInput = template.toByteArray().inputStream(),
            templateParams = { mapOf("name" to ParamValue.constant("World")) }
        ).inputStream().readBytes().let {
            String(it)
        }

        assertEquals("Hello World!", result)
    }
}