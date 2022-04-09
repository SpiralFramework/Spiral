package info.spiralframework.formats.common.audio

public data class HcaAudioChannel(
        val type: Int,
        val codedScalefactorCount: Int,
        val hfrScaleIndex: Int
) {
    val intensity: ByteArray = ByteArray(HighCompressionAudio.HCA_SUBFRAMES_PER_FRAME)
    val scaleFactors: ByteArray = ByteArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val resolution: ByteArray = ByteArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)

    val gain: FloatArray = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val spectra: FloatArray = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val temp: FloatArray = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val dct: FloatArray = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)
    val imdct_previous: FloatArray = FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME)

    val wave: Array<FloatArray> = Array(HighCompressionAudio.HCA_SUBFRAMES_PER_FRAME) { FloatArray(HighCompressionAudio.HCA_SAMPLES_PER_SUBFRAME) }
}

public data class HcaAudioInfo(
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

public data class HcaVariableRateInfo(
        val maxFrameSize: Int,
        val noiseLevel: Int
)

public data class HcaAbsoluteThresholdHearingInfo(
        val type: Int
)

public data class HcaLoopInfo(
        val startFrame: Int,
        val endFrame: Int,
        val startDelay: Int,
        val endPadding: Int
)

public data class HcaCipherInfo(
        val type: Int
)

public data class HcaRelativeVolumeAdjustmentInfo(
        val volume: Float
)

public data class HcaCommentInfo(
        val comment: String
)