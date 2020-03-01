package info.spiralframework.core.formats.video

//object SFLFormat: ReadableSpiralFormat<SFL> {
//    /** A **RECOGNISABLE** name, not necessarily the full name. May commonly be the extension */
//    override val name: String = "SFL"
//    override val extension: String = "sfl"
//
//    /**
//     * Attempts to read the data source as [T]
//     *
//     * @param name Name of the data, if any
//     * @param game Game relevant to this data
//     * @param context Context that we retrieved this file in
//     * @param source A function that returns an input stream
//     *
//     * @return a FormatResult containing either [T] or null, if the stream does not contain the data to form an object of type [T]
//     */
//    override suspend fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource<*>): FormatResult<SFL> {
//        val sfl = SFL(context, source) ?: return FormatResult.Fail(this, 1.0)
//        return FormatResult.Success(this, sfl, 1.0)
//    }
//}