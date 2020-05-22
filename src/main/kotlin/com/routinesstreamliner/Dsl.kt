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

    fun <K : Any> map(transform: (T) -> K) : ParamValue<K> {
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
}

class Routines(private val args: Array<String> = emptyArray()) {
    private val _routines = arrayListOf<Routine>()
    private val _params = ArrayList<ParamValue<Any>>()

    val routines: List<Routine> = _routines

    fun addRoutine(r: Routine) {
        _routines.add(r)
    }

    fun paramFromStdin(hint: String = "Input value: "): ParamValue<String> {
        return addGlobalParam { ParamValue.stdin(hint = hint) }
    }

    fun <T : Any> addGlobalParam(p: () -> ParamValue<T>): ParamValue<T> {
        val param = p()
        _params.add(param)
        return param
    }

    fun initAllParams() {
        _params.forEach { it.eagerInit() }
    }

    fun execute() {
        println("\n--- Routines Report ---")
        println("--- ${routines.size} routine(s) found ---")

        routines.asSequence().filter {
            println(it.createReportHeader())
            it.willExecute()
        }.forEach {
            it.execute()
            println(it.createExecutionReport())
        }
    }
}

private fun printMenuAndGetInput(r: Routines): String {
    println("\n-------------------------")
    println("Pick routine(s) to execute:")
    println(" q ::: To exit")
    println("#0 ::: Execute all routines")
    println()
    r.routines.forEachIndexed { index, routine ->
        println("#${index + 1} ::: ${routine.friendlyName()}")
    }

    return ParamValue.stdin("Enter your choice (number): ").get()
}

fun routines(args: Array<String> = emptyArray(), block: Routines.() -> Unit) {
    val repeat = args.contains("repeat")
    do {
        val r = Routines(args)
        r.block()

        val inp =  printMenuAndGetInput(r)
        if (inp == "q") return

        val ix: Int? = inp.toIntOrNull() ?: continue

        if (ix == 0) {
            r.initAllParams()
            r.execute()
        } else {
            r.routines[ix!! - 1].execute()
        }
    } while (repeat)
}