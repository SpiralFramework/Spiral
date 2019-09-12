package info.spiralframework.formats.common.audio

@ExperimentalUnsignedTypes
data class HcaAudioChannel(
        val type: Int,
        val codedScalefactorCount: Int,
        val hfrScaleIndex: Int
) {
    val intensity = ByteArray(HighCompressionAudio.HCA_SUBFRAMES_PER_FRAME)
    val scaleFactors = ByteArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val resolution = ByteArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)

    val gain = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val spectra = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val temp = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val dct = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val imdct_previous = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)

    val wave = Array(HighCompressionAudio.HCA_SUBFRAMES_PER_FRAME) { FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME) }
}

data class HcaAudioInfo(
        val frameSize: Int,
        val minResolution: Int,
        val maxResolution: Int,
        val trackCount: Int,
        val channelConfig: Int,
        val totalBandCount: Int,
        val baseBandCount: Int,
        val stereoBandCount: Int,
        val bandsPerHfrGroup: Int,
        val reserved1: Int? = null,
        val reserved2: Int? = null
)

data class HcaVariableRateInfo(
        val maxFrameSize: Int,
        val noiseLevel: Int
)

data class HcaAbsoluteThresholdHearingInfo(
        val type: Int
)

data class HcaLoopInfo(
        val startFrame: Int,
        val endFrame: Int,
        val startDelay: Int,
        val endPadding: Int
)

data class HcaCipherInfo(
        val type: Int
)

data class HcaRelativeVolumeAdjustmentInfo(
        val volume: Float
)

data class HcaCommentInfo(
        val comment: String
)