package com.michibaum.evolutionsimulation.brain.senses

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.brain.actions.Direction
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.Tile

interface Sense{
    fun calc(world: World,organism: Organism, currentTile: Tile): Double
}

fun allSenses() : List<() -> Sense> = listOf(
    { VisionSense(Direction.UP) },
    { VisionSense(Direction.DOWN) },
    { VisionSense(Direction.LEFT) },
    { VisionSense(Direction.RIGHT) },
    { HearingSense() },
    { SmellSense(null) },
    { SmellSense(Direction.UP) },
    { SmellSense(Direction.DOWN) },
    { SmellSense(Direction.LEFT) },
    { SmellSense(Direction.RIGHT) },
    { TasteSense() },
    { TouchSense() },
    { HungerSense() },
    { HealthSense() }
)