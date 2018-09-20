package org.abimon.spiral.core.objects.game.hpa

import org.abimon.spiral.core.utils.DataHandler
import org.abimon.spiral.core.utils.castToTypedArray

object SharedHPA {
    val itemNames: Map<String, Array<String>> =
            DataHandler.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("item_names.json"))
                    ?.mapValues { (_, value) -> value?.castToTypedArray<String>() ?: emptyArray() }
                    ?: emptyMap()

    val trialCameraNames: Map<String, Array<String>> =
            DataHandler.readMapFromStream(DR1::class.java.classLoader.getResourceAsStream("trial_cameras.json"))
                    ?.mapValues { (_, value) -> value?.castToTypedArray<String>() ?: emptyArray() }
                    ?: emptyMap()
}