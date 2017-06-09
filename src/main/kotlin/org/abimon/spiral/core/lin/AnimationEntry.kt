package org.abimon.spiral.core.lin

data class AnimationEntry(val id: Int, val arg3: Int, val arg4: Int, val arg5: Int, val arg6: Int, val arg7: Int, val frame: Int): LinScript {
    constructor(id: Int, frame: Int): this(id, 0, 0, 0, 0, 0, frame)
    constructor(main: Int, rem: Int, frame: Int): this(main * 256 + rem, frame)
    constructor(main: Int, rem: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, frame: Int): this(main * 256 + rem, arg3, arg4, arg5, arg6, arg7, frame)
    
    override fun getOpCode(): Int = 0x06
    override fun getRawArguments(): IntArray = intArrayOf(id / 256, id % 256, arg3, arg4, arg5, arg6, arg7, frame)
}