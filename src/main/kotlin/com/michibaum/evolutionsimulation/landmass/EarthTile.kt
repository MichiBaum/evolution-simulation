package com.michibaum.evolutionsimulation.landmass

import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.food.Eatable

class EarthTile(
    override val location_x: Int,
    override val location_y: Int,
    override var food: Eatable? = null,
    override var organism: Organism? = null,
) : Tile {

}