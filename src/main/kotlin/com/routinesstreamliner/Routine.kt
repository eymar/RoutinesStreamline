package com.routinesstreamliner

interface Routine<T> {
    val friendlyName: String

    val dependencies: List<ParamValue<*>>

    val willExecute: ParamValue<Boolean>

    val execute: () -> T

    open class Builder<T> {
        protected var friendlyName: String? = null
        protected var willExecute: ParamValue<Boolean> = ParamValue.constant(true)
        protected val dependencies = arrayListOf<ParamValue<*>>()
        protected lateinit var routineBody: () -> T

        fun friendlyName(f: () -> String) {
            this.friendlyName = f()
        }

        fun dependsOn(vararg params: ParamValue<*>) {
            this.dependencies.addAll(params)
        }

        fun executableIf(f: () -> ParamValue<Boolean>) {
            this.willExecute = f()
            dependencies.add(this.willExecute)
        }

        fun routineBody(f: () -> T) {
            this.routineBody = f
        }

        open fun build(): Routine<T> {
            return RoutineImpl(
                friendlyName = friendlyName ?: "Routine's friendly name not defined!",
                dependencies = dependencies,
                willExecute = willExecute,
                execute = routineBody
            )
        }
    }
}

private class RoutineImpl<T>(
    override val friendlyName: String,
    override val dependencies: List<ParamValue<*>>,
    override val willExecute: ParamValue<Boolean>,
    override val execute: () -> T
) : Routine<T>