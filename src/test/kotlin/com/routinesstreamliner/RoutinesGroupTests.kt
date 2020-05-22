package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutinesGroupTests {

    @Test
    fun `gn the same routine instance added a few times, it should be executed only once`() {
        val group = RoutinesGroup()

        var executeCounter = 0

        val r = object : Routine() {
            override fun execute() {
                executeCounter += 1
            }
        }

        group.apply {
            +r
            +r
            +r
        }.execute()


        assertEquals(1, executeCounter)
    }

    @Test
    fun `gn group with different routines, should execute all of them`() {
        val group = RoutinesGroup()

        var executeCounter1 = 0
        val r1 = object : Routine() {
            override fun execute() {
                executeCounter1 += 1
            }
        }

        var executeCounter2 = 0
        val r2 = object : Routine() {
            override fun execute() {
                executeCounter2 += 1
            }
        }

        var executeCounter3 = 0
        val r3 = object : Routine() {
            override fun execute() {
                executeCounter3 += 1
            }
        }

        group.apply {
            +r1
            +r2
            +r3
        }.execute()


        assertEquals(1, executeCounter1)
        assertEquals(1, executeCounter2)
        assertEquals(1, executeCounter3)
    }

    @Test
    fun `routine's global group should contain all child groups routines and execute them`() {
        val routines = Routines()

        var r1e = false
        val r1 = object : Routine() {
            override fun execute() {
                r1e = true
            }
        }

        var r2e = false
        val r2 = object : Routine() {
            override fun execute() {
                r2e = true
            }
        }

//        routines.addRoutine(r1)
//        routines.addRoutine(r2)

        routines.group {
            +r1
        }

        routines.group {
            +r2
        }

        routines.routinesGroups[0].execute()

        assertTrue(r1e)
        assertTrue(r2e)
    }


    @Test
    fun `should execute only routines of a chosen group`() {
        val routines = Routines()

        var r1e = 0
        val r1 = object : Routine() {
            override fun execute() {
                r1e += 1
            }
        }

        var r2e = 0
        val r2 = object : Routine() {
            override fun execute() {
                r2e += 1
            }
        }

//        routines.addRoutine(r1)
//        routines.addRoutine(r2)

        routines.group {
            +r1
        }

        routines.group {
            +r2
        }

        routines.routinesGroups[1].execute()

        assertEquals(1, r1e)
        assertEquals(0, r2e)

        routines.routinesGroups[2].execute()

        assertEquals(1, r1e)
        assertEquals(1, r2e)

        routines.routinesGroups[0].execute()

        assertEquals(2, r1e)
        assertEquals(2, r2e)
    }

}