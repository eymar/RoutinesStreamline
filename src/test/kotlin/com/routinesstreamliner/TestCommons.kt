package com.routinesstreamliner

internal fun testRoutinesContext(run: (ParamsStore) -> Unit) {
    ParamsStore().also {
        ParamsStore.setInstance(it)
        run(it)
        ParamsStore.reset()
    }
}