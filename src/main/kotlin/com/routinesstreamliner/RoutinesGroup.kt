package com.routinesstreamliner

class RoutinesGroup(
    val friendlyName: String,
    val routines: List<Routine<*>>
)

class RoutinesGroupBuilder {
    private var friendlyName: String? = null
    private val routines = arrayListOf<Routine<*>>()

    fun friendlyName(f: () -> String) {
        friendlyName = f()
    }

    fun addRoutine(r: Routine<*>) {
        routines.add(r)
    }

    operator fun Routine<*>.unaryPlus() {
        addRoutine(this)
    }

    fun build(): RoutinesGroup {
        return RoutinesGroup(
            friendlyName = friendlyName ?: routines.joinToString(separator = " | ") {
                it.friendlyName
            },
            routines = routines
        )
    }
}