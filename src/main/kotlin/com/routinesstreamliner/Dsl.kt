package com.routinesstreamliner

internal fun executeGroup(g: RoutinesGroup) {
    g.routines.also { list ->
        list.asSequence().flatMap {
            it.dependencies.asSequence()
        }.forEach {
            it.eagerInit()
        }

        list.distinctBy {
            System.identityHashCode(it)
        }.forEach {
            it.execute()
        }
    }
}

private fun printMenuAndGetInput(r: Routines): String {
    println("\n-------------------------")
    println("Pick routines group to routineBody:")
    println("#0 ::: To quit")

    r.groups.forEachIndexed { index, routine ->
        println("#${index + 1} ::: ${routine.friendlyName}")
    }

    return ParamValue.stdin("Enter your choice (number): ").get()
}

fun routines(args: Array<String> = emptyArray(), block: RoutinesBuilder.() -> Unit) {
    val repeat = args.contains("repeat")
    do {
        val r = RoutinesBuilder.create(block)

        val inp = printMenuAndGetInput(r)
        val ix: Int? = inp.toIntOrNull() ?: continue
        if (ix == 0) {
            return
        }
        executeGroup(r.groups[ix!! - 1])
    } while (repeat)
}