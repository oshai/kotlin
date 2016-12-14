// WITH_RUNTIME
// WITH_COROUTINES
class Controller {
    suspend fun suspendHere(v: String): String = suspendWithCurrentContinuation { x ->
        x.resume(v)

        SUSPENDED
    }

    // INTERCEPT_RESUME_PLACEHOLDER
}

fun builder(c: @Suspend() (Controller.() -> Unit)) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

inline fun run(block: () -> Unit) {
    block()
}

fun box(): String {
    var result = ""
    run {
        builder {
            try {
                result += suspendHere("O")
            } finally {
                result += suspendHere("K")
            }
        }
    }

    return result
}
