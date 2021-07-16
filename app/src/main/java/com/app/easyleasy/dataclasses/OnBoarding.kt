package com.app.easyleasy.dataclasses

import com.app.easyleasy.R

data class OnBoarding(
    val image: Int,
    val title: String,
    val description: String
)

val boardingFirstScreen = OnBoarding(
    image = R.drawable.test,
    title = "Easy Payment",
    description = "Avoid unnecessary school visits with online payment and application."
)

val boardingSecondScreen = OnBoarding(
    image = R.drawable.test,
    title = "Daily Reports",
    description = "Stay Updated with notice, homework and announcement given by teacher."
)

val boardingThirdScreen = OnBoarding(
    image = R.drawable.test,
    title = "Stay Updated",
    description = "Keep track of attendance, bus route, result and much more."
)

val onBoardings = arrayListOf(
    boardingFirstScreen, boardingSecondScreen, boardingThirdScreen)