package org.abimon.spiral.core.objects.game

/**
 * The Danganronpa Games all share similar properties, which can be accessed here
 * This is only used as a form of abstraction.
 */
interface DRGame {
    val names: Array<String>
    val steamID: String?
}