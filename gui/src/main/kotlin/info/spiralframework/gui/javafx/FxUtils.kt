package info.spiralframework.gui.javafx

import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableValue
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import javafx.collections.ObservableList
import javafx.css.Styleable
import javafx.scene.Node
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.javafx.StackedFontIcon
import kotlin.reflect.KProperty

public inline fun <T : Styleable> T.withStyles(vararg classes: String): T {
    styleClass.addAll(classes)
    return this
}

inline fun fontIcon(iconCode: String, iconSize: Int): FontIcon {
    val icon = FontIcon(iconCode)
    icon.iconSize = iconSize
    return icon
}

inline fun fontIcon(ikon: Ikon, iconSize: Int): FontIcon {
    val icon = FontIcon(ikon)
    icon.iconSize = iconSize
    return icon
}

inline fun ObservableList<in FontIcon>.addFontIcon(iconCode: String, iconSize: Int) {
    val icon = FontIcon(iconCode)
    icon.iconSize = iconSize
    this.add(icon)
}

inline fun ObservableList<in FontIcon>.addFontIcon(ikon: Ikon, iconSize: Int) {
    val icon = FontIcon(ikon)
    icon.iconSize = iconSize
    this.add(icon)
}

inline fun stackedFontIcon(iconSize: Int? = null): StackedFontIcon {
    val icon = StackedFontIcon()
    if (iconSize != null) {
        icon.iconSize = iconSize
    }
    return icon
}

inline fun stackedFontIcon(iconSize: Int? = null, builder: ObservableList<Node>.() -> Unit): StackedFontIcon {
    val icon = StackedFontIcon()

    if (iconSize != null) {
        icon.iconSize = iconSize
    }

    icon.children.builder()

    return icon
}

public inline operator fun <T> ObservableValue<T>.getValue(thisRef: Any?, property: KProperty<*>): T =
    value

//public inline operator fun <T> WritableValue<T>.getValue(thisRef: Any?, property: KProperty<*>): T =
//    value

public inline operator fun <T> WritableValue<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    setValue(value)
}

public inline fun <E> baseListChangeListener(
    crossinline onPermutations: (Change<out E>) -> Unit = {},
    crossinline onUpdates: (Change<out E>) -> Unit = {},
    crossinline onRemoved: (Change<out E>) -> Unit = {},
    crossinline onAdded: (Change<out E>) -> Unit = {},
): ListChangeListener<E> =
    ListChangeListener { change ->
        while (change.next()) {
            if (change.wasPermutated()) {
                onPermutations(change)
            } else if (change.wasUpdated()) {
                onUpdates(change)
            } else {
                onRemoved(change)
                onAdded(change)
            }
        }
    }


//@PublishedApi
//internal val ON_PERMUTATIONS_DEFAULT: (ObservableList<*>, movedFrom: Int, movedTo: Int) -> Unit = { _, _, _ -> }
//
//public inline fun <E> listChangeListener(
//    crossinline onPermutations: (ObservableList<E>, movedFrom: Int, movedTo: Int) -> Unit = ON_PERMUTATIONS_DEFAULT,
//    crossinline onUpdates: (Change<out E>) -> Unit = {},
//    crossinline onRemoved: (Change<out E>) -> Unit = {},
//    crossinline onAdded: (Change<out E>) -> Unit = {},
//): ListChangeListener<E> =
//    ListChangeListener { change ->
//        while (change.next()) {
//            if (change.wasPermutated()) {
//                onPermutations(change)
//            } else if (change.wasUpdated()) {
//                onUpdates(change)
//            } else {
//                onRemoved(change)
//                onAdded(change)
//            }
//        }
//    }