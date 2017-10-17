package org.abimon.spiral.core.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.abimon.spiral.core.TripleHashMap
import org.abimon.spiral.core.put
import org.abimon.visi.lang.make

object SpiralData {
    val dr1OpCodes = make<TripleHashMap<Int, Int, String>> {
        put(0x00, 2, "Text Count")
        put(0x01, 3, "0x01")
        put(0x02, 2, "Text")
        put(0x03, 1, "Format")
        put(0x04, 4, "Filter")
        put(0x05, 2, "Movie")
        put(0x06, 8, "Animation")
        put(0x07, -1, "0x07")
        put(0x08, 5, "Voice Line")
        put(0x09, 3, "Music")
        put(0x0A, 3, "SFX A")
        put(0x0B, 2, "SFX B")
        put(0x0C, 2, "Truth Bullet")
        put(0x0D, 3, "0x0D")
        put(0x0E, 2, "0x0E")
        put(0x0F, 3, "Set Title")
        put(0x10, 3, "Set Report Info")
        put(0x11, 4, "0x11")
        put(0x12, -1, "0x12")
        put(0x13, -1, "0x13")
        put(0x14, 3, "Trial Camera")
        put(0x15, 3, "Load Map")
        put(0x16, -1, "0x16")
        put(0x17, -1, "0x17")
        put(0x18, -1, "0x18")
        put(0x19, 3, "Script")
        put(0x1A, 0, "Stop Script")
        put(0x1B, 3, "Run Script")
        put(0x1C, 0, "0x1C")
        put(0x1D, -1, "0x1D")
        put(0x1E, 5, "Sprite")
        put(0x1F, 7, "0x1F")
        put(0x20, 5, "0x20")
        put(0x21, 1, "Speaker")
        put(0x22, 3, "0x22")
        put(0x23, 5, "0x23")
        put(0x24, -1, "0x24")
        put(0x25, 2, "Change UI")
        put(0x26, 3, "Set Flag")
        put(0x27, 1, "Check Character")
        put(0x28, -1, "0x28")
        put(0x29, 1, "Check Object")
        put(0x2A, 2, "Set Label")
        put(0x2B, 1, "Choice")
        put(0x2C, 2, "0x2C")
        put(0x2D, -1, "0x2D")
        put(0x2E, 2, "0x2E")
        put(0x2F, 10, "0x2F")
        put(0x30, 3, "Show Background")
        put(0x31, -1, "0x31")
        put(0x32, 1, "0x32")
        put(0x33, 4, "0x33")
        put(0x34, 2, "Goto Label")
        put(0x35, -1, "Check Flag A")
        put(0x36, -1, "Check Flag B")
        put(0x37, -1, "0x37")
        put(0x38, -1, "0x38")
        put(0x39, 5, "0x39")
        put(0x3A, 0, "Wait For Input")
        put(0x3B, 0, "Wait Frame")
        put(0x3C, 0, "End Flag Check")
    }
    val dr2OpCodes = make<TripleHashMap<Int, Int, String>> {
        put(0x00, 2, "Text Count")
        put(0x01, 4, "0x01")
        put(0x02, 2, "Text")
        put(0x03, 1, "Format")
        put(0x04, 4, "Filter")
        put(0x05, 2, "Movie")
        put(0x06, 8, "Animation")
        put(0x07, -1, "0x07")
        put(0x08, 5, "Voice Line")
        put(0x09, 3, "Music")
        put(0x0A, 3, "SFX A")
        put(0x0B, 2, "SFX B")
        put(0x0C, 2, "Truth Bullet")
        put(0x0D, 3, "0x0D")
        put(0x0E, 2, "0x0E")
        put(0x0F, 3, "Set Title")
        put(0x10, 3, "Set Report Info")
        put(0x11, 4, "0x11")
        put(0x12, -1, "0x12")
        put(0x13, -1, "0x13")
        put(0x14, 6, "Trial Camera")
        put(0x15, 4, "Load Map")
        put(0x16, -1, "0x16")
        put(0x17, -1, "0x17")
        put(0x18, -1, "0x18")
        put(0x19, 5, "Script")
        put(0x1A, 0, "Stop Script")
        put(0x1B, 5, "Run Script")
        put(0x1C, 0, "0x1C")
        put(0x1D, -1, "0x1D")
        put(0x1E, 5, "Sprite")
        put(0x1F, 7, "0x1F")
        put(0x20, 5, "0x20")
        put(0x21, 1, "Speaker")
        put(0x22, 3, "0x22")
        put(0x23, 5, "0x23")
        put(0x24, -1, "0x24")
        put(0x25, 2, "Change UI")
        put(0x26, 3, "Set Flag")
        put(0x27, 1, "Check Character")
        put(0x28, -1, "0x28")
        put(0x29, 1, "Check Object")
        put(0x2A, 2, "Set Label")
        put(0x2B, 1, "Choice")
        put(0x2C, 2, "0x2C")
        put(0x2D, -1, "0x2D")
        put(0x2E, 5, "0x2E")
        put(0x2F, 10, "0x2F")
        put(0x30, 3, "Show Background")
        put(0x31, -1, "0x31")
        put(0x32, 1, "0x32")
        put(0x33, 4, "0x33")
        put(0x34, 2, "Goto Label")
        put(0x35, -1, "Check Flag A")
        put(0x36, -1, "Check Flag B")
        put(0x37, -1, "0x37")
        put(0x38, -1, "0x38")
        put(0x39, 5, "0x39")
        put(0x3A, 4, "Wait For Input DR1")
        put(0x3B, 2, "Wait Frame DR1")
        put(0x3C, 0, "End Flag Check")
        put(0x4B, 0, "Wait For Input")
        put(0x4C, 0, "Wait Frame")
    }

    val drv3OpCodes = make<TripleHashMap<Int, Int, String>> {
        put(0x00, 4, "Set Flag") //Value, Flag
        put(0x10, 4, "Script")
        put(0x14, 2, "Label")
        put(0x17, 8, "Animation")
        put(0x19, 4, "Voice")
        put(0x1A, 6, "Music") //Track, Play, Fade
        put(0x1D, 2, "Speaker")
        put(0x46, 2, "Text")
        put(0x47, 0, "Wait For Input")
        put(0x53, 2, "Speaker")
        put(0x58, 2, "Text")
    }

    val nonstopOpCodes = hashMapOf(
            0x00 to "TextID",
            0x01 to "Type",
            0x03 to "Shoot With Evidence",
            0x06 to "Has Weak Point",
            0x07 to "Advance",
            0x0A to "Transition",
            0x0B to "Fadeout",
            0x0C to "Horizontal",
            0x0D to "Vertical",
            0x0E to "Angle Acceleration",
            0x0F to "Angle",
            0x10 to "Scale",
            0x11 to "Final Scale",
            0x13 to "Rotation",
            0x14 to "Rotation Speed",
            0x15 to "Character",
            0x16 to "Sprite",
            0x17 to "Background Animation",
            0x19 to "Voice",
            0x1B to "Chapter"
    )

    val MAPPER: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    val YAML_MAPPER: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
    val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"
    val SPIRAL_HEADER_NAME = "Spiral-Header"
}