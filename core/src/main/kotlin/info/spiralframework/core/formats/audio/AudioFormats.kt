package info.spiralframework.core.formats.audio

object AudioFormats {
    val DEFAULT_WAV = object: SpiralAudioFormat("wav") {}
    val DEFAULT_OGG = object: SpiralAudioFormat("ogg") {}
    val DEFAULT_MP3 = object: SpiralAudioFormat("mp3") {}
    var wav = DEFAULT_WAV
    var ogg = DEFAULT_OGG
    var mp3 = DEFAULT_MP3
}