// WITH_RUNTIME
// WITH_COROUTINES
class Controller {
    var ok = false
    var v  = "fail"
    suspend fun suspendHere(v: String): Unit = suspendWithCurrentContinuation { x ->
        this.v = v
        x.resume(Unit)
        SUSPENDED
    }
}

fun builder(c: @Suspend() (Controller.() -> Unit)): String {
    val controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.ok = true
    })
    if (!controller.ok) throw RuntimeException("Fail 1")
    return controller.v
}

fun box(): String {

    return builder {
        suspendHere("OK")
    }
}
