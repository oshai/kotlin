// CHECK_NOT_CALLED_IN_SCOPE: function=plus_za3lpa$ scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=plus scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=invoke scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=get_za3lpa$ scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=set_vux9f0$ scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=dec scope=box
// CHECK_NOT_CALLED_IN_SCOPE: function=minus_za3lpa$ scope=box

class A {
    inline operator fun plus(a: Int) = a + 10
}

class B

inline operator fun B.plus(b: Int) = b + 20

object O {
    inline operator fun invoke() = 42
}

object R {
    inline operator fun get(i: Int) = 99 + i
}

object S {
    var lastResult = 0

    inline operator fun set(i: Int, value: Int) {
        lastResult = i + value
    }
}

class N(val value: Int) {
    inline operator fun minus(other: Int) = N(value - other)

    inline operator fun dec() = N(value - 1)
}

fun box(): String {
    var result = A() + 1
    if (result != 11) return "fail: member operator"

    result = B() + 2
    if (result != 22) return "fail: extension operator"

    result = O()
    if (result != 42) return "fail: invoke operator"

    result = R[1]
    if (result != 100) return "fail: get operator"

    S[2] = 3
    result = S.lastResult
    if (result != 5) return "fail: set operator"

    var n = N(10)
    n--
    if (n.value != 9) return "fail: decrement"
    n -= 3
    if (n.value != 6) return "fail: augmented assignment"

    return "OK"
}