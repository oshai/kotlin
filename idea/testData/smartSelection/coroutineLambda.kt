fun foo(coroutine f: Int.() -> Continuation<Unit>) {

}

fun test() {
    foo <caret>{}
}

/*
foo {}
*/