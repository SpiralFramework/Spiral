package info.spiralframework.core.formats.compression


//interface CompressionFormat<T: ICompression>: ReadableSpiralFormat<DataSource> {
//    val compressionFormat: T
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
//    override fun read(context: SpiralContext, readContext: FormatReadContext?, source: DataSource): FormatResult<DataSource> {
//        if (compressionFormat.isCompressed(context, source)) {
//            val tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".dat")
//            tmpFile.deleteOnExit()
//
//            tmpFile.outputStream().use { out -> compressionFormat.decompressToPipe(context, source, out) }
//
//            val result = FormatResult.Success<DataSource>(this, tmpFile::inputStream, 1.0)
//            result.release.add(Closeable { tmpFile.delete() })
//            return result
//        }
//
//        return FormatResult.Fail(this, 1.0)
//    }
//}