package com.michibaum.evolutionsimulation.brain

interface Sense

class VisionSense(val direction: Direction) : Sense {
    override fun toString(): String = "VisionSense $direction"
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
    { VisionSense(Direction.UP) },
    { VisionSense(Direction.DOWN) },
    { VisionSense(Direction.LEFT) },
    { VisionSense(Direction.RIGHT) },
    { HearingSense() },
    { SmellSense() },
    { TasteSense() },
    { TouchSense() },
    { HungerSense() }
)