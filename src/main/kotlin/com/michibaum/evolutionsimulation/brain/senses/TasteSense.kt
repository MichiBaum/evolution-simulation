package com.michibaum.evolutionsimulation.brain.senses

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.Tile

class TasteSense : Sense {
    override fun calc(world: World, organism: Organism, currentTile: Tile): Double {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "TasteSense"
}