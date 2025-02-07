package com.michibaum.evolutionsimulation.brain

interface Sense

class VisionSense : Sense {
    override fun toString(): String = "VisionSense"
}

class HearingSense : Sense {
    override fun toString(): String = "HearingSense"
}

class SmellSense : Sense {
    override fun toString(): String = "SmellSense"
}

class TasteSense : Sense {
    override fun toString(): String = "TasteSense"
}

class TouchSense : Sense {
    override fun toString(): String = "TouchSense"
}

class HungerSense : Sense {
    override fun toString(): String = "HungerSense"
}

fun allSenses() : List<() -> Sense> = listOf(
    { VisionSense() },
    { HearingSense() },
    { SmellSense() },
    { TasteSense() },
    { TouchSense() },
    { HungerSense() }
)