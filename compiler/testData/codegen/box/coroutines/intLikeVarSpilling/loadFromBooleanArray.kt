// WITH_RUNTIME
// WITH_COROUTINES
class Controller {
    suspend fun suspendHere(): Unit = suspendWithCurrentContinuation { x ->
        x.resume(Unit)
        SUSPENDED
    }

    // INTERCEPT_RESUME_PLACEHOLDER
}

fun builder(c: @Suspend() (Controller.() -> Unit)) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

private var booleanResult = false
fun setBooleanRes(x: Boolean) {
    booleanResult = x
}

fun box(): String {
    builder {
        val a = booleanArrayOf(true)
        val x = a[0]
        suspendHere()
        setBooleanRes(x)
    }

    if (!booleanResult) return "fail 1"

    return "OK"
}
