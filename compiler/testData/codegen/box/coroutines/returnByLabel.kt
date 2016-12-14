// WITH_RUNTIME
// WITH_COROUTINES
suspend fun suspendHere(): String = suspendWithCurrentContinuation { x ->
    x.resume("OK")
    SUSPENDED
}

fun builder(c: @Suspend() (() -> Int)): Int {
    var res = 0
    c.startCoroutine(handleResultContinuation {
        res = it
    })

    return res
}

fun box(): String {
    var result = ""

    val handledResult = builder {
        result = suspendHere()
        return@builder 56
    }

    if (handledResult != 56) return "fail 1: $handledResult"

    return result
}
