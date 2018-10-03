package org.abimon.spiral.core.objects.scripting.lin

interface LinTextScript {
    var text: String?
    var textID: Int

    val writeBOM: Boolean
}