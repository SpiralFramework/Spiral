package info.spiralframework.base.binding

import info.spiralframework.base.common.locale.CommonLocale
import info.spiralframework.base.common.locale.LocaleBundle

actual class DefaultLocaleBundle actual constructor(bundleName: String, locale: CommonLocale) : LocaleBundle {
    override val bundleName: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val locale: CommonLocale
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override var parent: LocaleBundle?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override suspend fun loadWithLocale(locale: CommonLocale): LocaleBundle? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val entries: Set<Map.Entry<String, String>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: Set<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val size: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: Collection<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun containsKey(key: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsValue(value: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}