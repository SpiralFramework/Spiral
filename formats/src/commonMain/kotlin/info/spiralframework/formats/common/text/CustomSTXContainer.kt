package info.spiralframework.formats.common.text

@ExperimentalUnsignedTypes
class CustomSTXContainer {
    private val _strings: MutableMap<Int, String> = HashMap()
    val strings: Map<Int, String>
        get() = _strings

    var language: STXContainer.Language = STXContainer.Language.JPLL
    var languageID: Int
        get() = language.languageID
        set(value) {
            language = STXContainer.Language(value)
        }

    var performExistingStringOptimisation = true

    fun add(string: String): Int {
        //First, check if the string already exists
        if (performExistingStringOptimisation) {
            _strings.forEach { (existingID, str) ->
                if (string == str) {
                    return existingID
                }
            }
        }

        //IntelliJ Bug
        @Suppress("ReplaceManualRangeWithIndicesCalls")
        for (i in 0 until _strings.size) {
            if (i !in _strings) {
                _strings[i] = string
                return i
            }
        }
        val id = _strings.size
        _strings[id] = string
        return id
    }
    fun add(stringID: Int, string: String) = set(stringID, string)
    operator fun set(stringID: Int, string: String) {
        _strings[stringID] = string
    }

    fun addAll(newStrings: Array<String>): IntArray {
        val stringIDs = IntArray(newStrings.size) { -1 }

        //First, check if the strings already exists
        if (performExistingStringOptimisation) {
            _strings.forEach { (existingID, str) ->
                newStrings.forEachIndexed { index, newStr ->
                    if (newStr == str) {
                        stringIDs[index] = existingID
                    }
                }
            }
        }

        var previousIndex = -1

        newStrings.forEachIndexed { index, newStr ->
            if (stringIDs[index] != -1)
                return@forEachIndexed

            while (++previousIndex < _strings.size) {
                if (previousIndex !in _strings) {
                    break
                }
            }

            _strings[previousIndex] = newStr
            stringIDs[index] = previousIndex
        }

        return stringIDs
    }

    fun addAll(newStrings: Collection<String>): IntArray {
        val stringIDs = IntArray(newStrings.size) { -1 }

        //First, check if the strings already exists
        if (performExistingStringOptimisation) {
            _strings.forEach { (existingID, str) ->
                newStrings.forEachIndexed { index, newStr ->
                    if (newStr == str) {
                        stringIDs[index] = existingID
                    }
                }
            }
        }

        var previousIndex = -1

        newStrings.forEachIndexed { index, newStr ->
            if (stringIDs[index] != -1)
                return@forEachIndexed

            while (++previousIndex < _strings.size) {
                if (previousIndex !in _strings) {
                    break
                }
            }

            _strings[previousIndex] = newStr
            stringIDs[index] = previousIndex
        }

        return stringIDs
    }
}