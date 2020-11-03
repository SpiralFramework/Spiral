package info.spiralframework.console.jvm.commands.data

import info.spiralframework.base.common.SpiralContext
import info.spiralframework.base.common.locale.localiseOrNull

data class HelpDetails(val key: String, val name: String?, val blurb: String?, val desc: String?, val usage: String?, val cmd: String?) {
    companion object {
        private val SEPARATOR_CHARACTERS = "[_\\- ]".toRegex()
        fun sanitiseFunctionIdentifier(name: String): String = name.toUpperCase().replace(SEPARATOR_CHARACTERS, "")

        public inline operator fun invoke(context: SpiralContext, key: String) =
            HelpDetails(
                key = key,
                name = context.localiseOrNull("help.$key.name"),
                blurb = context.localiseOrNull("help.$key.blurb"),
                desc = context.localiseOrNull("help.$key.desc"),
                usage = context.localiseOrNull("help.$key.usage"),
                cmd = context.localiseOrNull("help.$key.cmd")
            ).takeUnless { details -> details.name == null && details.blurb == null && details.desc == null && details.usage == null && details.cmd == null }
    }

    val cmdKey: String? by lazy { cmd?.let(HelpDetails::sanitiseFunctionIdentifier) }

    operator fun component7() = cmdKey

    @PublishedApi
    internal constructor(context: SpiralContext, key: String, ignored: Boolean): this(
        key = key,
        name = context.localiseOrNull("help.$key.name"),
        blurb = context.localiseOrNull("help.$key.blurb"),
        desc = context.localiseOrNull("help.$key.desc"),
        usage = context.localiseOrNull("help.$key.usage"),
        cmd = context.localiseOrNull("help.$key.cmd")
    )

    fun copyWithUpdate(context: SpiralContext): HelpDetails =
        HelpDetails(
            key = key,
            name = name ?: context.localiseOrNull("help.$key.name"),
            blurb = blurb ?: context.localiseOrNull("help.$key.blurb"),
            desc = desc ?: context.localiseOrNull("help.$key.desc"),
            usage = usage ?: context.localiseOrNull("help.$key.usage"),
            cmd = cmd ?: context.localiseOrNull("help.$key.cmd")
        )
}