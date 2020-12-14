package m.kampukter.travelexpenses.data

sealed class SomeSealed(id: String) {

    data class Foo(val id: String, val something: String) : SomeSealed(id)

    data class Bar(val id: String, val somethingElse: Int) : SomeSealed(id)

}
