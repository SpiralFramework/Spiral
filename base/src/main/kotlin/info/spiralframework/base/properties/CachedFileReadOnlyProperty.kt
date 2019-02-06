package info.spiralframework.base.properties

import info.spiralframework.base.util.sha512Hash
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CachedFileReadOnlyProperty<in R, out T>(val file: File, val op: (File) -> T): ReadOnlyProperty<R, T> {
    private var hash: String
    private var value: T
     /**
     * Returns the value of the property for the given object.
     * @param thisRef the object for which the value is requested.
     * @param property the metadata for the property.
     * @return the property value.
     */
    override fun getValue(thisRef: R, property: KProperty<*>): T {
         val currentHash = file.sha512Hash()
         if (currentHash != hash) {
             hash = currentHash
             value = op(file)
         }

         return value
    }

    fun File.sha512Hash(): String = FileInputStream(this).use(InputStream::sha512Hash)

    init {
        if (!file.exists())
            file.createNewFile()

        hash = file.sha512Hash()
        value = op(file)
    }
}