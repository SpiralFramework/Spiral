package info.spiralframework.formats.common.games

/**
 * The Danganronpa Games all share similar properties, which can be accessed here
 * This is only used as a form of abstraction.
 */
interface DRGame {
    val names: Array<String>
    val identifier: String
        get() = names.firstOrNull() ?: "none"
    /**
     * A map of the colour to the internal clt name or number
     */
    val colourCodes: Map<String, String>
    val steamID: String?
}