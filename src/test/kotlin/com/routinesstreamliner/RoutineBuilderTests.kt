package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutineBuilderTests {

    @Test
    fun `builder should create a routine with given fields`() {
        val r1p1 = ParamValue.constant(1)
        val r1p2 = ParamValue.constant("1")
        val r1executable = ParamValue.constant(true)
        val r1body = {
            r1p1.get() + r1p2.get().toInt()
        }

        val r1 = Routine.Builder<Int>().apply {
            friendlyName { "r1" }
            dependsOn(r1p1, r1p2)
            executableIf { r1executable }
            routineBody(r1body)
        }.build()

        assertEquals("r1", r1.friendlyName)
        assertTrue {
            r1.dependencies.containsAll(listOf(r1p1, r1p2, r1executable))
        }
        assertEquals(true, r1.willExecute.get())
        assertEquals(2, r1.execute())
    }

    @Test
    fun `by default builder should create a routine with willExecute = true, and default friendlyName`() {
        val r1body = { 0 }

        val r1 = Routine.Builder<Int>().apply {
            routineBody(r1body)
        }.build()

        assertEquals(true, r1.willExecute.get())
        assertTrue(!r1.friendlyName.isBlank())
    }
}