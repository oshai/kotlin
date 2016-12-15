/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.jvm.internal

import kotlin.coroutines.*

private const val INTERCEPT_BIT_SET = 1 shl 31
private const val INTERCEPT_BIT_CLEAR = INTERCEPT_BIT_SET.inv()

@SinceKotlin("1.1")
abstract class CoroutineImpl : RestrictedCoroutineImpl, DispatchedContinuation<Any?> {
    private val _dispatcher: ContinuationDispatcher?

    override val dispatcher: ContinuationDispatcher?
        get() = _dispatcher

    // this constructor is used to create a continuation instance for coroutine
    constructor(arity: Int, completion: Continuation<Any?>?) : super(arity, completion) {
        _dispatcher = (completion as? DispatchedContinuation<*>)?.dispatcher
    }

    override fun resume(value: Any?) {
        if (_dispatcher != null) {
            if (label and INTERCEPT_BIT_SET == 0) {
                label = label or INTERCEPT_BIT_SET
                if (_dispatcher.interceptResume(value, this)) return
            }
            label = label and INTERCEPT_BIT_CLEAR
        }
        super.resume(value)
    }

    override fun resumeWithException(exception: Throwable) {
        if (_dispatcher != null) {
            if (label and INTERCEPT_BIT_SET == 0) {
                label = label or INTERCEPT_BIT_SET
                if (_dispatcher.interceptResumeWithException(exception, this)) return
            }
            label = label and INTERCEPT_BIT_CLEAR
        }
        super.resumeWithException(exception)
    }
}

@SinceKotlin("1.1")
abstract class RestrictedCoroutineImpl : Lambda, Continuation<Any?> {
    @JvmField
    protected var resultContinuation: Continuation<Any?>?

    // label == -1 when coroutine cannot be started (it is just a factory object) or has already finished execution
    // label == 0 in initial part of the coroutine
    @JvmField
    protected var label: Int

    // this constructor is used to create a continuation instance for coroutine
    constructor(arity: Int, resultContinuation: Continuation<Any?>?) : super(arity) {
        this.resultContinuation = resultContinuation
        label = if (resultContinuation != null) 0 else -1
    }

    override fun resume(value: Any?) {
        try {
            val result = doResume(value, null)
            if (result != CoroutineIntrinsics.SUSPENDED)
                resultContinuation!!.resume(result)
        } catch (e: Throwable) {
            resultContinuation!!.resumeWithException(e)
        }
    }

    override fun resumeWithException(exception: Throwable) {
        try {
            val result = doResume(null, exception)
            if (result != CoroutineIntrinsics.SUSPENDED)
                resultContinuation!!.resume(result)
        } catch (e: Throwable) {
            resultContinuation!!.resumeWithException(e)
        }
    }

    protected abstract fun doResume(data: Any?, exception: Throwable?): Any?
}

/*
===========================================================================================================================
Showcase of coroutine compilation strategy.

Given this following "sample" suspend function code:

---------------------------------------------------------------------------------------------
@RestrictsSuspendExtensions
class Receiver {
    suspend fun yield(): SomeResult
}

suspend fun Receiver.sample(): String {
    doSomethingBefore()
    val yieldResult = yield() // suspension point
    doSomethingAfter(yieldResult)
    return "Done"
}
---------------------------------------------------------------------------------------------

The compiler emits the class with the following logic:

---------------------------------------------------------------------------------------------

class XXXCoroutine : RestrictedCoroutineImpl, Function2<Receiver, Continuation<*>, Any?> {
    //               ^^^^^^^^^^^^^^^^^^^^^^^
    //          Replace with CoroutineImpl if the coroutine is non-restricted

    val receiver: Receiver?
    //  ^^^^^^^^^^^^^^^^^^^
    //    receiver is just a part of the captured scope and is only declared here is coroutine has it

    // this constructor is used to create initial "factory" lambda object
    constructor() : super(2, null) {
        this.receiver = null
    }

    // this constructor is used to create a continuation instance for coroutine
    constructor(receiver: Receiver, resultContinuation: Continuation<Any?>) : super(2, resultContinuation) {
    //         ^^^^^^^^^^^^^^^^^^^^
    // no receiver parameter when compiling coroutine that does not have one
        this.receiver = receiver
    }

    override fun doCreate(receiver: Any, resultContinuation: Continuation<*>): Continuation<Unit> {
        return XXXCoroutine(receiver as Receiver, resultContinuation) // create the actual instance
    //                      ^^^^^^^^^^^^^^^^^^^^
    // ignore receiver parameter when compiling coroutine that does not have one,
    // check that it is not null as check its proper type for coroutine that has a receiver
    }

    override fun doResume(data: Any?, exception: Throwable?): Any? {
        var suspensionResult = data
        switch (label) {
        default:
            throw IllegalStateException()
        case 0:
            doSomethingBefore()
            label = 1 // set next label before calling suspend function
            suspensionResult = receiver.yield(this)
            if (suspensionResult == SUSPENDED) return SUSPENDED
            // falls through with yeild result if it is available
        case 1:
            doSomethingAfter(supensionResult)
            label = -1 // execution is over
            return "Done" // we have result of execution
        }
    }
}
---------------------------------------------------------------------------------------------
*/
