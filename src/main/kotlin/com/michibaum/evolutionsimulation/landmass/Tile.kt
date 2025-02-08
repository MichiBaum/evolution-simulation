package com.michibaum.evolutionsimulation.landmass

import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.food.Eatable

interface Tile {

    val location_x: Int
    val location_y: Int

    var food: Eatable?
    fun hasFood(): Boolean = food != null

    var organism: Organism?
    fun hasOrganisms(): Boolean = organism != null

}