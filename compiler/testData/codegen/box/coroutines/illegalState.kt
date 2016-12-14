// WITH_RUNTIME
// WITH_COROUTINES
// TARGET_BACKEND: JVM
// NO_INTERCEPT_RESUME_TESTS
suspend fun suspendHere(): Unit = suspendWithCurrentContinuation { x ->
    x.resume(Unit)
    SUSPENDED
}

fun builder1(c: @Suspend() (() -> Unit)) {
    (c as Continuation<Unit>).resume(Unit)
}

fun builder2(c: @Suspend() (() -> Unit)) {
    val continuation = c.createCoroutine(EmptyContinuation)
    val declaredField = continuation.javaClass.superclass.superclass.getDeclaredField("label")
    declaredField.setAccessible(true)
    declaredField.set(continuation, -3)
    continuation.resume(Unit)
}

fun box(): String {

    try {
        builder1 {
            suspendHere()
        }
        return "fail 1"
    } catch (e: kotlin.KotlinNullPointerException) {
    }

    try {
        builder2 {
            suspendHere()
        }
        return "fail 3"
    } catch (e: java.lang.IllegalStateException) {
        if (e.message != "call to 'resume' before 'invoke' with coroutine") return "fail 4: ${e.message!!}"
    }

    var result = "OK"

    try {
        builder1 {
            result = "fail 5"
        }
        return "fail 6"
    } catch (e: kotlin.KotlinNullPointerException) {
    }

    try {
        builder2 {
            result = "fail 8"
        }
        return "fail 9"
    } catch (e: java.lang.IllegalStateException) {
        if (e.message != "call to 'resume' before 'invoke' with coroutine") return "fail 10: ${e.message!!}"
        return result
    }

    return "fail"
}
