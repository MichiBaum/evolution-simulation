package com.michibaum.evolutionsimulation.brain.senses

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.Tile

class HealthSense : Sense {
    override fun calc(world: World, organism: Organism, currentTile: Tile): Double {
        return when {
            organism.energy > 60 -> 1.5
            organism.energy > 10 -> 1.0
            organism.energy <= 0 -> -5.0
            else -> 0.0
        }
    }

    override fun toString(): String = "HealthSense"
}