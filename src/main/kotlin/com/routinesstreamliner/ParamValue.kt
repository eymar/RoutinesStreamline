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
            return ParamValue { value }.eagerInit()
        }

        /**
         * @param validate - validator, should throw a Throwable if validation fails.
         * Use [require] with lazyMessage to add validation rules.
         * If validation fails, it will loop till validation passes:  get input, validate input.
         */
        fun stdin(hint: String = "Input value: ", validate: (String) -> Unit = {}): ParamValue<String> {
            return ParamValue {
                var input: String
                var validation: Throwable?

                do {
                    print("\n$hint ")
                    input = readLine()!!

                    validation = runCatching {
                        validate(input)
                    }.exceptionOrNull()
                } while (validation != null)

                input
            }
        }
    }
}