package com.michibaum.evolutionsimulation.brain.senses

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.Tile

class HungerSense : Sense {
    override fun calc(world: World, organism: Organism, currentTile: Tile): Double {
        return if (organism.energy < 60) -20.0 else 1.0
    }

    override fun toString(): String = "HungerSense"
}