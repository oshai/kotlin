// EXTRACTION_TARGET: property with getter
fun foo(coroutine f: Int.() -> Continuation<Unit>) {

}

fun test() {
    foo <selection>{}</selection>
}