package info.spiralframework.media

import info.spiralframework.core.formats.audio.AudioFormats

object HumbleMediaPlugin {
    @JvmStatic
    fun main(args: Array<String>) {
        AudioFormats.wav = HumbleAudioFormat("wav")
        AudioFormats.ogg = HumbleAudioFormat("ogg")
        AudioFormats.mp3 = HumbleAudioFormat("mp3")
    }
}