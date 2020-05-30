package com.routinesstreamliner

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParamStoreTests {

    @Test
    fun `gn Params values, store should return correct getters`() {
        val store = ParamsStore()
        ParamsStore.setInstance(store)

        val p1 = ParamValue { 1 }
        val p2 = ParamValue { "a" }
        val p3 = ParamValue { 5.0 }

        assertEquals(1, store.get(p1).invoke())
        assertEquals(1, p1.get())

        assertEquals("a", store.get(p2).invoke())
        assertEquals("a", p2.get())

        assertEquals(5.0, store.get(p3).invoke())
        assertEquals(5.0, p3.get())
    }

    @Test
    fun `store can be replaced`() {
        val store = ParamsStore()
        ParamsStore.setInstance(store)

        val p1 = ParamValue { 1 }
        assertEquals(1, store.get(p1).invoke())


        ParamsStore.setInstance(ParamsStore())
        assertTrue {
            kotlin.runCatching { p1.get() }.isFailure
        }
    }
}