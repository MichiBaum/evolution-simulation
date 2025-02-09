package com.michibaum.evolutionsimulation.brain.senses

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.brain.actions.Direction
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.Tile

class SmellSense(val direction: Direction?) : Sense {
    override fun calc(world: World, organism: Organism, currentTile: Tile): Double {
        fun wrapCoordinate(coord: Int, max: Int): Int = (coord + max) % max
        val maxX = world.tiles.size  // Width of the world
        val maxY = world.tiles[0].size  // Height of the world
        when (direction) {
            Direction.LEFT -> {
                val wrappedX = wrapCoordinate(currentTile.location_x - 1, maxX)
                return if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
            }
            Direction.RIGHT -> {
                val wrappedX = wrapCoordinate(currentTile.location_x + 1, maxX)
                return if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
            }
            Direction.UP -> {
                val wrappedY = wrapCoordinate(currentTile.location_y - 1, maxY)
                return if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
            }
            Direction.DOWN -> {
                val wrappedY = wrapCoordinate(currentTile.location_y + 1, maxY)
                return if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
            }
            null -> return if (currentTile is EarthTile && currentTile.food != null) 1.0 else 0.0
        }
    }

    override fun toString(): String = "SmellSense $direction"
}