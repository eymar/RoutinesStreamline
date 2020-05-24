package com.routinesstreamliner

import org.junit.Assert.*
import org.junit.Test
import kotlin.test.assertTrue

class RoutinesBuilderTests {

    private fun dumbRoutine(execute: () -> Unit) = Routine.Builder<Unit>().apply {
        friendlyName { "" }
        routineBody(execute)
    }.build()

    @Test
    fun `should create and add a group of all added routines`() {
        val r1 = dumbRoutine { }
        val r2 = dumbRoutine { }

        val rs = RoutinesBuilder.create {
            addRoutine(r1)
            addRoutine(r2)
        }

        assertEquals(1, rs.groups.size)
        assertTrue {
            rs.groups[0].routines.containsAll(listOf(r1, r2))
        }
    }

    @Test
    fun `should add all given groups and should add their routines to the global group`() {
        val r1 = dumbRoutine { }
        val r2 = dumbRoutine { }

        val rs = RoutinesBuilder.create {
            group(groupName = "g1") {
                +r1
            }
            group(groupName = "g2") {
                +r2
            }
        }

        assertEquals(3, rs.groups.size)

        assertTrue {
            rs.groups[0].routines.containsAll(listOf(r1, r2))
        }

        assertTrue {
            rs.groups[1].routines.containsAll(listOf(r1))
        }

        assertTrue {
            rs.groups[2].routines.containsAll(listOf(r2))
        }
    }
}