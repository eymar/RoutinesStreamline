package com.routinesstreamliner

open class ParamValue<out T : Any>(
    private val valueGetter: (() -> T)
) {

    private lateinit var value: T

    fun get(): T = if (::value.isInitialized) {
        value
    } else {
        value = valueGetter()
        value
    }

    override fun toString(): String {
        return value.toString()
    }

    fun <K : Any> map(transform: (T) -> K): ParamValue<K> {
        return ParamValue {
            transform(get())
        }
    }

    fun eagerInit(): ParamValue<T> {
        get()
        return this
    }

    companion object {

        fun <T : Any> constant(value: T): ParamValue<T> {
            return ParamValue { value }
        }

        fun stdin(hint: String = "Input value: "): ParamValue<String> {
            return ParamValue {
                print("\n" + hint)
                readLine()!!
            }
        }
    }
}

@DslMarker
annotation class RoutinesMarker

@RoutinesMarker
abstract class Routine {
    private var executableIf: () -> ParamValue<Boolean> = { ParamValue { true } }

    protected open var _friendlyName: () -> String = {
        this.javaClass.simpleName
    }

    private val _params = arrayListOf<ParamValue<*>>()

    val paramsDependencies: List<ParamValue<*>> = _params

    fun executableIf(explanation: String = "", condition: () -> ParamValue<Boolean>) {
        this.executableIf = condition
    }

    fun willExecute(): Boolean = this.executableIf().get()

    abstract fun execute()

    open fun createReportHeader(): String {
        return "\nRoutine [" + friendlyName() + "], willExecute = ${willExecute()}"
    }

    open fun createExecutionReport(): String = ""

    open fun friendlyName(): String = _friendlyName()

    fun friendlyName(name: () -> String) {
        _friendlyName = name
    }

    fun dependsOn(vararg params: ParamValue<*>) {
        _params.addAll(params)
    }
}

class RoutinesGroup {
    private val list = arrayListOf<Routine>()

    val routines: List<Routine> = list

    private var _friendlyName: () -> String = {
        list.joinToString { it.friendlyName() }
    }

    fun groupFriendlyName(name: () -> String) {
        _friendlyName = name
    }

    fun groupFriendlyName(): String = _friendlyName()

    fun execute() {
        // Init all required params before start
        list.asSequence().flatMap {
            it.paramsDependencies.asSequence()
        }.forEach {
            it.eagerInit()
        }

        // Execute
        list.distinctBy {
            System.identityHashCode(it)
        }.forEach {
            it.execute()
        }
    }

    fun add(r: Routine) {
        list.add(r)
    }

    fun addAll(list: Collection<Routine>) {
        list.forEach { add(it) }
    }

    operator fun Routine.unaryPlus(): Routine {
        add(this)
        return this
    }
}

class Routines(private val args: Array<String> = emptyArray()) {
    private val _routineGroups = arrayListOf<RoutinesGroup>()

    private val globalGroup = RoutinesGroup().also {
        it.groupFriendlyName { "All routines" }
        _routineGroups.add(it)
    }

    val routinesGroups: List<RoutinesGroup> = _routineGroups

    fun addRoutine(r: Routine) {
        globalGroup.add(r)
    }

    fun paramFromStdin(hint: String = "Input value: "): ParamValue<String> {
        return ParamValue.stdin(hint = hint)
    }

    fun group(groupName: String? = null, configure: RoutinesGroup.() -> Unit) {
        RoutinesGroup().also {
            if (groupName != null) {
                it.groupFriendlyName { groupName }
            }
            configure(it)
            _routineGroups.add(element = it)
            globalGroup.addAll(it.routines)
        }
    }
}

private fun printMenuAndGetInput(r: Routines): String {
    println("\n-------------------------")
    println("Pick routines group to execute:")
    println("#0 ::: To quit")

    r.routinesGroups.forEachIndexed { index, routine ->
        println("#${index + 1} ::: ${routine.groupFriendlyName()}")
    }

    return ParamValue.stdin("Enter your choice (number): ").get()
}

fun routines(args: Array<String> = emptyArray(), block: Routines.() -> Unit) {
    val repeat = args.contains("repeat")
    do {
        val r = Routines(args)
        r.block()

        val inp = printMenuAndGetInput(r)
        val ix: Int? = inp.toIntOrNull() ?: continue
        if (ix == 0) {
            return
        }
        r.routinesGroups[ix!! - 1].execute()
    } while (repeat)
}