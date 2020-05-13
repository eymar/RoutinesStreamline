package com.routinesstreamliner

class ParamValue(
    private val valueGetter: (() -> String)
) {

    constructor(paramValue: ParamValue): this(paramValue::get)

    private var value: String? = null

    fun get() = value ?: valueGetter().also { value = it }

    override fun toString(): String {
        return get()
    }

    operator fun plus(str: String): ParamValue {
        val c = this
        return ParamValue { c.get() + str }
    }

    operator fun plus(v: ParamValue): ParamValue {
        val c = this
        return ParamValue { c.get() + v.get() }
    }

    companion object {

        fun constant(value: String): ParamValue {
            return ParamValue { value }
        }

        fun combine(paramValue: () -> ParamValue) : ParamValue {
            return ParamValue(paramValue())
        }

        fun stdin(hint: String = "Input value: "): ParamValue {
            return ParamValue {
                print(hint)
                readLine()!!
            }
        }
    }
}

@DslMarker
annotation class RoutinesMarker

@RoutinesMarker
abstract class Routine(parentParams: Map<String, ParamValue> = emptyMap()) {

    protected val routineParams = HashMap<String, ParamValue>(parentParams)
    private var executableIf: (Map<String, ParamValue>) -> Boolean = { true }


    fun executableIf(condition: (Map<String, ParamValue>) -> Boolean) {
        this.executableIf = condition
    }

    fun willExecute() = this.executableIf(routineParams)

    fun addRoutineParams(vararg args: Pair<String, ParamValue>) {
        routineParams.putAll(args)
    }

    abstract fun execute()

    open fun createReportHeader(): String {
        return "\nRoutine [" + this.javaClass.simpleName + "], willExecute = ${willExecute()}"
    }

    open fun createExecutionReport(): String = ""
}

class Routines {
    private val _routines = arrayListOf<Routine>()
    private val _params = HashMap<String, ParamValue>()

    val params: Map<String, ParamValue> = _params
    val routines: List<Routine> = _routines

    fun globalParams(vararg args: Pair<String, ParamValue>) {
        _params.putAll(args)
    }

    fun <T : Routine> addRoutine(r: T) {
        _routines.add(r)
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

fun routines(block: Routines.() -> Unit) {
    val r = Routines()
    r.block()
    r.execute()
}