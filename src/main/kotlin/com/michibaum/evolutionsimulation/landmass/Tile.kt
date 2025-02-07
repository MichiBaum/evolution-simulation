package com.michibaum.evolutionsimulation.landmass

import com.michibaum.evolutionsimulation.food.Eatable

interface Tile {

    var food: Eatable?
    fun hasFood(): Boolean = food != null

}