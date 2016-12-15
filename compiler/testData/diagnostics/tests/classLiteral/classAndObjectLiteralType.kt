import kotlin.reflect.KClass

abstract class Base<T : Any>(val klass: KClass<out T>)

class DerivedClass : Base<DerivedClass>(DerivedClass::class)

class DerivedObject : Base<DerivedObject>(DerivedObject::class)