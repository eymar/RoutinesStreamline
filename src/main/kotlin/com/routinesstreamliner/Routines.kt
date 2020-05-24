package com.routinesstreamliner

class Routines(
    val groups: List<RoutinesGroup>
)

class RoutinesBuilder {

    private val routines = arrayListOf<Routine<*>>()
    private val groups = arrayListOf<RoutinesGroup>()

    fun addRoutine(r: Routine<*>) {
        routines.add(r)
    }

    fun group(groupName: String, b: RoutinesGroupBuilder.() -> Unit) {
        val gb = RoutinesGroupBuilder().apply(b)
        gb.friendlyName { groupName }
        groups.add(gb.build())
    }

    fun build(): Routines {
        val allTasksGroup = RoutinesGroupBuilder().apply {
            routines.forEach { this.addRoutine(it) }
            groups.asSequence().flatMap {
                it.routines.asSequence()
            }.forEach {
                this.addRoutine(it)
            }
            friendlyName { "Default: all routines" }
        }.build()

        groups.add(0, allTasksGroup)

        return Routines(groups)
    }

    companion object {
        fun create(block: RoutinesBuilder.() -> Unit): Routines {
            return RoutinesBuilder().apply(block).build()
        }
    }
}