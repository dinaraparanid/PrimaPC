package com.paranid5.prima.presentation.ui.theme

import androidx.compose.ui.graphics.Color as ComposeColor

sealed class Theme(
    private val r: Int,
    private val g: Int,
    private val b: Int,
    @JvmField val isNight: Boolean
) {
    /** Gets rgb of theme */
    val rgb get() = ComposeColor(r, g, b)

    class Purple : Theme(110, 60, 255, false)
    class Red : Theme(230, 90, 125, false)
    class Blue : Theme(30, 45, 210, false)
    class Green : Theme(25, 160, 40, false)
    class Orange : Theme(225, 135, 15, false)
    class Lemon : Theme(190, 225, 15, false)
    class Turquoise : Theme(15, 225, 200, false)
    class GreenTurquoise : Theme(15, 225, 150, false)
    class Sea : Theme(15, 210, 225, false)
    class Pink : Theme(220, 15, 225, false)

    class PurpleNight : Theme(110, 60, 255, true)
    class RedNight : Theme(160, 25, 30, true)
    class BlueNight : Theme(30, 45, 210, true)
    class GreenNight : Theme(25, 160, 40, true)
    class OrangeNight : Theme(225, 135, 15, true)
    class LemonNight : Theme(190, 225, 15, true)
    class TurquoiseNight : Theme(15, 225, 200, true)
    class GreenTurquoiseNight : Theme(15, 225, 150, true)
    class SeaNight : Theme(15, 210, 225, true)
    class PinkNight : Theme(220, 15, 225, true)
}
