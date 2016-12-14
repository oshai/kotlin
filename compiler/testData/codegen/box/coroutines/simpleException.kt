// WITH_RUNTIME
// WITH_COROUTINES
class Controller {
    suspend fun suspendHere(): String = suspendWithCurrentContinuation { x ->
        x.resumeWithException(RuntimeException("OK"))
        SUSPENDED
    }

    // INTERCEPT_RESUME_PLACEHOLDER
}

fun builder(c: @Suspend() (Controller.() -> Unit)) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

fun box(): String {
    var result = ""

    builder {
        try {
            suspendHere()
            result = "fail"
        } catch (e: RuntimeException) {
            result = "OK"
        }
    }

    return result
}
