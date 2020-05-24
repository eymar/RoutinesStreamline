package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoutinesGroupTests {

    private fun dumbRoutine(execute: () -> Unit) = Routine.Builder<Unit>().apply {
        friendlyName { "" }
        routineBody(execute)
    }.build()

    @Test
    fun `should build RoutineGroup with all fields`() {
        val r1 = dumbRoutine {  }
        val r2 = dumbRoutine {  }
        val g = RoutinesGroupBuilder().apply {
            +r1
            +r2
            friendlyName { "g1" }
        }.build()

        assertEquals("g1", g.friendlyName)
        assertTrue {
            g.routines.containsAll(listOf(r1, r2))
        }
    }

    @Test
    fun `gn the same routine instance added a few times, it should be executed only once`() {
        val group = RoutinesGroupBuilder()

        var executeCounter = 0

        val r = dumbRoutine {
            executeCounter += 1
        }

        group.apply {
            +r
            +r
            +r
        }.also {
            executeGroup(it.build())
        }


        assertEquals(1, executeCounter)
    }

    @Test
    fun `gn group with different routines, should execute all of them`() {
        val group = RoutinesGroupBuilder()

        var executeCounter1 = 0
        val r1 = dumbRoutine {
            executeCounter1 += 1
        }

        var executeCounter2 = 0
        val r2 = dumbRoutine {
            executeCounter2 += 1
        }

        var executeCounter3 = 0
        val r3 = dumbRoutine {
            executeCounter3 += 1
        }

        group.apply {
            +r1
            +r2
            +r3
        }.also {
            executeGroup(it.build())
        }


        assertEquals(1, executeCounter1)
        assertEquals(1, executeCounter2)
        assertEquals(1, executeCounter3)
    }

    @Test
    fun `routine's global group should contain all child groups routines and execute them`() {
        val routines = RoutinesBuilder()

        var r1e = false
        val r1 = dumbRoutine {
            r1e = true
        }

        var r2e = false
        val r2 = dumbRoutine {
            r2e = true
        }

        routines.group(groupName = "g1") {
            +r1
        }

        routines.group(groupName = "g2") {
            +r2
        }

        executeGroup(routines.build().groups[0])

        assertTrue(r1e)
        assertTrue(r2e)
    }


    @Test
    fun `should execute only routines of a chosen group`() {
        val routines = RoutinesBuilder()

        var r1e = 0
        val r1 = dumbRoutine { r1e += 1 }

        var r2e = 0
        val r2 = dumbRoutine { r2e += 1 }

        routines.group(groupName = "g1") {
            +r1
        }

        routines.group(groupName = "g2") {
            +r2
        }

        val r = routines.build()

        executeGroup(r.groups[1])

        assertEquals(1, r1e)
        assertEquals(0, r2e)

        executeGroup(r.groups[2])

        assertEquals(1, r1e)
        assertEquals(1, r2e)

        executeGroup(r.groups[0])

        assertEquals(2, r1e)
        assertEquals(2, r2e)
    }

    @Test
    fun `should init dependencies before execution`() {
        var p1counter = 0
        var p2counter = 0

        var p1Time = Long.MAX_VALUE
        var p2Time = Long.MAX_VALUE
        var execTime = Long.MIN_VALUE

        val p1 = ParamValue {
            p1Time = System.currentTimeMillis()
            ++p1counter
        }
        val p2 = ParamValue {
            p2Time = System.currentTimeMillis()
            ++p2counter
        }

        val r = Routine.Builder<Unit>().apply {
            friendlyName { "" }
            dependsOn(p1, p2)
            routineBody {
                Thread.sleep(10)
                execTime = System.currentTimeMillis()
                p1.get() + p2.get()
            }
        }.build()

        val group = RoutinesGroupBuilder().apply {
            +r
        }.build()


        executeGroup(group)
        assertTrue(p1Time < execTime)
        assertEquals(1, p1counter)

        assertTrue(p2Time < execTime)
        assertEquals(1, p2counter)
    }
}