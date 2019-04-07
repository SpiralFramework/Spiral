package info.spiralframework.media

import info.spiralframework.core.formats.audio.AudioFormats
import info.spiralframework.core.plugins.BaseSpiralPlugin

object HumbleMediaPlugin: BaseSpiralPlugin(HumbleMediaPlugin::class, "spiralframework_media_plugin.yaml") {
    @JvmStatic
    fun main(args: Array<String>) {
        load()
    }

    override fun load() {
        println("Loading!")
        AudioFormats.wav = HumbleAudioFormat("wav")
        AudioFormats.ogg = HumbleAudioFormat("ogg")
        AudioFormats.mp3 = HumbleAudioFormat("mp3")
    }

    override fun unload() {
        println("Unloading!")
        AudioFormats.wav = AudioFormats.DEFAULT_WAV
        AudioFormats.ogg = AudioFormats.DEFAULT_OGG
        AudioFormats.mp3 = AudioFormats.DEFAULT_MP3
    }
}