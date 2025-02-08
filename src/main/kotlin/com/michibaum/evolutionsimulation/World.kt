package com.michibaum.evolutionsimulation

import com.michibaum.evolutionsimulation.brain.Brain
import com.michibaum.evolutionsimulation.utils.BrainGenerator
import com.michibaum.evolutionsimulation.creatures.LandOrganism
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.Tile
import com.michibaum.evolutionsimulation.landmass.WaterTile

data class World (
    val tiles: Array<Array<Tile>>,
){
    fun getTileAt(x: Int, y: Int): Tile {
        return tiles[x][y]
    }

    fun getOrganismAt(x: Int, y: Int): Organism? {
        return tiles[x][y].organism
    }

    fun setOrganismAt(x: Int, y: Int, organism: Organism?) {
        tiles[x][y].organism = organism
    }

    fun getAllOrganisms(): List<Organism> {
        return tiles.flatten().mapNotNull { it.organism }
    }

    fun getTilesWithOrganisms(): List<Tile> {
        return tiles.flatten().filter { it.organism != null }
    }

}


class WorldGenerator(){

    fun generateRandomWorldWithOrganisms(width: Int, height: Int): World {
        val tiles: Array<Array<Tile>> = Array(width) { x ->
            Array(height) { y ->
                if (Math.random() < 0.7)
                    EarthTile(location_x = x, location_y = y, organism = landOrganismOrNull())
                else
                    WaterTile(location_x = x, location_y = y)
            }
        }

        return World(tiles)
    }

    private fun landOrganismOrNull() = if (Math.random() < 0.5)
        createLandOrganism()
    else
        null

    private fun createLandOrganism(): LandOrganism{
        val brain = BrainGenerator().generateRandomBrain(
            numSenses = 5,
            numActions = 5,
            numInterneurons = 20,
            numMotorNeurons = 6
        )
        return object : LandOrganism {
            override val brain: Brain = brain
            override var health: Int = 100 // Start with 100 health
            override var energy: Int = 50  // Start with 50 energy
            override var age: Int = 0      // Start with age 0
            override val learningRate: Double = 0.2
        }
    }

}