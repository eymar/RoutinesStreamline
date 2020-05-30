package com.routinesstreamliner

internal fun executeGroup(g: RoutinesGroup, paramsStore: ParamsStore) {
    paramsStore.also { store ->
        g.paramsOverrides.forEach {
            store.add(it.paramValue, it.getter)
        }
    }

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
    println("Pick routines group to execute:")
    println("#0 ::: To quit")

    r.groups.forEachIndexed { index, routine ->
        println("#${index + 1} ::: ${routine.friendlyName}")
    }

    return ParamValue.stdin(hint = "Enter your choice (number): ", validate = {
        val ix = it.toInt()
        require(ix >= 0 && ix <= r.groups.size) {
            "Index out of bounds"
        }
    }).get()
}

fun routines(args: Array<String> = emptyArray(), block: RoutinesBuilder.() -> Unit) {
    val repeat = args.contains("repeat")
    do {
        val paramsStore = ParamsStore()
        ParamsStore.setInstance(paramsStore)

        val r = RoutinesBuilder.create(block = block)
        val ix = printMenuAndGetInput(r).toInt()
        if (ix == 0) {
            return
        }
        executeGroup(r.groups[ix - 1], paramsStore = paramsStore)
    } while (repeat)
}