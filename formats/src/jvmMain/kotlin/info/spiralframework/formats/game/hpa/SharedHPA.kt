package info.spiralframework.formats.game.hpa

import info.spiralframework.formats.utils.DataHandler
import info.spiralframework.formats.utils.castToTypedArray

object SharedHPA {
    val itemNames: Map<String, Array<String>> =
            DataHandler.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("item_names.json"))
                    ?.mapValues { (_, value) -> value?.castToTypedArray<String>() ?: emptyArray() }
                    ?: emptyMap()

    val trialCameraNames: Map<String, Array<String>> =
            DataHandler.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("trial_cameras.json"))
                    ?.mapValues { (_, value) -> value?.castToTypedArray<String>() ?: emptyArray() }
                    ?: emptyMap()

    val evidenceNames: Map<String, Array<String>> =
            DataHandler.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("evidence_names.json"))
                    ?.mapValues { (_, value) -> value?.castToTypedArray<String>() ?: emptyArray() }
                    ?: emptyMap()
}