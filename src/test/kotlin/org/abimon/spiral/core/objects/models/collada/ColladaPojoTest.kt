package org.abimon.spiral.core.objects.models.collada

import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.data.SpiralData.XML_MAPPER
import org.junit.Test
import java.io.File

internal class ColladaPojoTest {
    @Test
    fun test() {
        val cube = SpiralData.XML_MAPPER.readValue(File("/Volumes/Lots of Data/V3 Pinecone/dae/car.dae"), ColladaPojo::class.java)
        XML_MAPPER.writeValue(File("/Volumes/Lots of Data/V3 Pinecone/dae/jcar.dae"), cube)
    }
}