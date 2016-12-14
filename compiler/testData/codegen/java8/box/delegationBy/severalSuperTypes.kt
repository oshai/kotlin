public interface Base {
    fun getValue(): String

    fun test() = getValue()
}


public abstract class MyClass : Base {
    override fun test() = "OK"

}


class Fail : Base {
    override fun getValue() = "Fail"
}

fun box(): String {
    val z = object : MyClass(), Base by Fail() {
        override fun getValue() = "Fail"
    }
    return z.test()
}
