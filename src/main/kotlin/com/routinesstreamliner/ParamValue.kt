package com.routinesstreamliner

class ParamValue<out T : Any>(
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