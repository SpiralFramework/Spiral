package info.spiralframework.formats.scripting.lin

interface LinTextScript {
    var text: String?
    var textID: Int

    val writeBOM: Boolean
}