package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DslTests {

    @Test
    fun `should execute the default (all) routines group when input is 1`() = testRoutinesContext {
        val input = "1\n" // 1 is an input for Default Group
        System.setIn(input.byteInputStream())

        var routine1Executed = false
        var routine2Executed = false

        val routine1 = Routine.Builder<Unit>().apply {
            friendlyName { "r1" }
            routineBody {
                routine1Executed = true
            }
        }.build()

        val routine2 = Routine.Builder<Unit>().apply {
            friendlyName { "rw" }
            routineBody {
                routine2Executed = true
            }
        }.build()

        routines {
            addRoutine(routine1)
            addRoutine(routine2)
        }

        assertTrue {
            routine1Executed && routine2Executed
        }
    }

    @Test
    fun `should execute only routines of a selected group`() = testRoutinesContext {
        val input = "2\n" // 2 is an input for custom group "g1"
        System.setIn(input.byteInputStream())

        var routine1Executed = false
        var routine2Executed = false

        val routine1 = Routine.Builder<Unit>().apply {
            friendlyName { "r1" }
            routineBody {
                routine1Executed = true
            }
        }.build()

        val routine2 = Routine.Builder<Unit>().apply {
            friendlyName { "rw" }
            routineBody {
                routine2Executed = true
            }
        }.build()

        routines {
            addRoutine(routine1)
            addRoutine(routine2)

            group(groupName = "g1") {
                +routine2
            }
        }

        assertFalse { routine1Executed }
        assertTrue { routine2Executed }
    }

    @Test
    fun `gn repeat mode, should execute the routines correct number of times`() = testRoutinesContext {
        val input = "2\n2\n1\n0\n" // 2 times for group "g1", 1 time for default (all) group, then 0 (quit)
        System.setIn(input.byteInputStream())

        var routine1Counter = 0
        var routine2Counter = 0

        val routine1 = Routine.Builder<Unit>().apply {
            friendlyName { "r1" }
            routineBody {
                routine1Counter += 1
            }
        }.build()

        val routine2 = Routine.Builder<Unit>().apply {
            friendlyName { "rw" }
            routineBody {
                routine2Counter += 1
            }
        }.build()

        routines(args = arrayOf("repeat")) {
            addRoutine(routine1)
            addRoutine(routine2)

            group(groupName = "g1") {
                +routine2
            }
        }

        assertEquals(1, routine1Counter)
        assertEquals(3, routine2Counter)
    }
}