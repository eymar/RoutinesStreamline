package com.routinesstreamliner

class RoutinesGroup(
    val friendlyName: String,
    val routines: List<Routine<*>>,
    val paramsOverrides: List<ParamOverride<Any>>
)

class ParamOverride<T : Any> internal constructor(
    val paramValue: ParamValue<T>,
    val getter: () -> T
)

class RoutinesGroupBuilder {
    private var friendlyName: String? = null
    private val routines = arrayListOf<Routine<*>>()
    private val paramsOverrides = arrayListOf<ParamOverride<Any>>()

    fun friendlyName(f: () -> String) {
        friendlyName = f()
    }

    fun addRoutine(r: Routine<*>) {
        routines.add(r)
    }

    fun <T : Any> overrideParam(paramValue: ParamValue<T>, newGetter: () -> T) {
        paramsOverrides.add(ParamOverride(paramValue, newGetter))
    }

    operator fun Routine<*>.unaryPlus() {
        addRoutine(this)
    }

    fun build(): RoutinesGroup {
        return RoutinesGroup(
            friendlyName = friendlyName ?: routines.joinToString(separator = " | ") {
                it.friendlyName
            },
            routines = routines,
            paramsOverrides = paramsOverrides
        )
    }
}