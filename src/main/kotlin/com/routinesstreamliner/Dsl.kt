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

    fun eagerInit(): ParamValue {
        if (value == null) {
            value = valueGetter()
        }
        return this
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

    protected open var _friendlyName: () -> String = {
        this.javaClass.simpleName
    }

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

    open fun friendlyName(): String = _friendlyName()

    fun friendlyName(name: () -> String) {
        _friendlyName = name
    }
}

class Routines(private val args: Array<String> = emptyArray()) {
    private val _routines = arrayListOf<Routine>()
    private val _params = ArrayList<ParamValue>()

    val params: List<ParamValue> = _params
    val routines: List<Routine> = _routines

//    fun globalParams(vararg args: Pair<String, ParamValue>) {
//        _params.putAll(args)
//    }

    fun addRoutine(r: Routine) {
        _routines.add(r)
    }

    fun inputParam(p: () -> String): ParamValue {
        return ParamValue(p).also {
            _params.add(it)
        }
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

    companion object {
        fun stdin(hint: String = "Input value: "): String {
            print(hint)
            return readLine()!!
        }
    }
}

fun routines(args: Array<String> = emptyArray(), block: Routines.() -> Unit) {
    val r = Routines(args)

    r.block()

    println("Pick routine(s) to execute:")
    println("#0 ::: Execute all routines")
    r.routines.forEachIndexed { index, routine ->
        println("#${index + 1} ::: ${routine.friendlyName()}")
    }

    val ix = ParamValue.stdin("Enter your choice (number): ").get().toInt()
    if (ix == 0) {
        r.initAllParams()
        r.execute()
    } else {
        r.routines[ix - 1].execute()
    }
}