package com.routinesstreamliner

import com.google.common.annotations.VisibleForTesting

internal class ParamsStore {

    private val store = hashMapOf<ParamValue<Any>, () -> Any>()

    internal fun <T : Any> add(paramValue: ParamValue<T>, getter: () -> T) {
        store[paramValue] = getter
    }

    internal fun <T : Any> get(paramValue: ParamValue<T>): () -> T {
        return store[paramValue] as () -> T
    }

    fun copy(): ParamsStore {
        val originalStore = store
        return ParamsStore().apply {
            originalStore.forEach { t, u ->
                this.add(t, u)
            }
        }
    }

    companion object {
        private var instance: ParamsStore? = null

        internal fun setInstance(store: ParamsStore) {
            instance = store
        }

        internal fun getInstance(): ParamsStore = instance!!

        @VisibleForTesting
        internal fun reset() {
            instance = null
        }
    }
}