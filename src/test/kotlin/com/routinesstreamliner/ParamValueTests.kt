package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals

class ParamValueTests {

    @Test
    fun `should create a parameter value from stdin`() = testRoutinesContext {
        val input = "10\n".byteInputStream()
        System.setIn(input)

        val p = ParamValue.stdin().map { it.toInt() }
        assertEquals(10, p.get())
    }

    @Test
    fun `should validate value from stdin and accept only a valid input`() = testRoutinesContext {
        val input = "0\n-1\n1".byteInputStream()
        System.setIn(input)

        var validateCounter = 0

        val p = ParamValue.stdin(validate = {
            validateCounter += 1
            require(it.toInt() > 0) { "Positive number excepted (>= 1)" }
        }).map { it.toInt() }

        assertEquals(1, p.get())
        assertEquals(3, validateCounter)
    }
}