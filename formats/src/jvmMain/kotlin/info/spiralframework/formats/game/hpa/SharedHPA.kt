package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.common.data.json.JsonGameStrings
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import java.io.InputStream

object SharedHPA {
    @UnstableDefault
    @ExperimentalStdlibApi
    val itemNames: JsonGameStrings =
            Json.parse(JsonGameStrings.serializer(), SharedHPA::class.java.classLoader
                    ?.getResourceAsStream("item_names.json")
                    ?.use(InputStream::readBytes)
                    ?.let(ByteArray::decodeToString)
                    ?: "{}"
            )

    @UnstableDefault
    @ExperimentalStdlibApi
    val trialCameraNames: JsonGameStrings =
            Json.parse(JsonGameStrings.serializer(), SharedHPA::class.java.classLoader
                    ?.getResourceAsStream("trial_cameras.json")
                    ?.use(InputStream::readBytes)
                    ?.let(ByteArray::decodeToString)
                    ?: "{}"
            )

    @UnstableDefault
    @ExperimentalStdlibApi
    val evidenceNames: JsonGameStrings =
            Json.parse(JsonGameStrings.serializer(), SharedHPA::class.java.classLoader
                    ?.getResourceAsStream("evidence_names.json")
                    ?.use(InputStream::readBytes)
                    ?.let(ByteArray::decodeToString)
                    ?: "{}"
            )
}