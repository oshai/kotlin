<error descr="[PLATFORM_DEFINITION_WITHOUT_DECLARATION] Modifier 'impl' is only applicable to members that are initially declared in platform-independent code">impl</error> class My {

    <error descr="[PLATFORM_DEFINITION_WITHOUT_DECLARATION] Modifier 'impl' is only applicable to members that are initially declared in platform-independent code">impl</error> fun foo() = 42
}

<error descr="[PLATFORM_DEFINITION_WITHOUT_DECLARATION] Modifier 'impl' is only applicable to members that are initially declared in platform-independent code">impl</error> class Your {

    <error descr="[PLATFORM_DEFINITION_WITHOUT_DECLARATION] Modifier 'impl' is only applicable to members that are initially declared in platform-independent code">impl</error> fun foo() = 13

    <error descr="[PLATFORM_DEFINITION_WITHOUT_DECLARATION] Modifier 'impl' is only applicable to members that are initially declared in platform-independent code">impl</error> fun bar(arg: Int) = arg

}

impl class His {

    impl fun foo() = 7

    impl fun bar(arg: Int) = arg == foo()

}