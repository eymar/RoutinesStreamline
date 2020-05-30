package com.routinesstreamliner

class ParamValue<out T : Any>(
    valueGetter: (() -> T)
) {

    init {
        ParamsStore.getInstance().add(this, valueGetter)
    }

    private lateinit var value: T

    fun get(): T = if (::value.isInitialized) {
        value
    } else {
        value = ParamsStore.getInstance().get(this).invoke()
        value
    }

    override fun toString(): String {
        return get().toString()
    }

    fun <R : Any> map(transform: (T) -> R): ParamValue<R> {
        return ParamValue {
            transform(get())
        }
    }

    fun <K : Any, R: Any> zipWith(
        z: ParamValue<K>,
        f: (T, K) -> R
    ): ParamValue<R> {
        return ParamValue { f(this.get(), z.get()) }
    }

    fun <K1 : Any, K2 : Any, R : Any> zipWith(
        z1: ParamValue<K1>,
        z2: ParamValue<K2>,
        f: (T, K1, K2) -> R
    ): ParamValue<R> {
        return ParamValue { f(this.get(), z1.get(), z2.get()) }
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