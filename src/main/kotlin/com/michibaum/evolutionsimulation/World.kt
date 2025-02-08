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

    fun generateRandomWorld(width: Int, height: Int, organisms: List<Organism>): World {
        val tiles: Array<Array<Tile>> = Array(width) { x ->
            Array(height) { y ->
                if (Math.random() < 0.7)
                    EarthTile(location_x = x, location_y = y)
                else
                    WaterTile(location_x = x, location_y = y)
            }
        }

        // Get all EarthTiles to assign organisms
        val earthTiles = tiles.flatten().filterIsInstance<EarthTile>()

        // Create enough unique organisms using deep copies
        val sufficientOrganisms = generateSufficientOrganisms(organisms, earthTiles.size)

        // Shuffle organisms for randomness
        val shuffledOrganisms = sufficientOrganisms.shuffled()

        // Assign organisms to EarthTiles
        earthTiles.zip(shuffledOrganisms).forEach { (tile, organism) ->
            tile.organism = organism
        }

        return World(tiles)
    }

    private fun generateSufficientOrganisms(originalOrganisms: List<Organism>, requiredCount: Int): List<Organism> {
        val organismCopies = mutableListOf<Organism>()

        // Keep adding deep copies of organisms until we have enough
        while (organismCopies.size < requiredCount) {
            organismCopies.addAll(originalOrganisms.map { it })
        }

        // Trim to the exact required count
        return organismCopies.take(requiredCount)
    }


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
            numInterneurons = 20,
            numMotorNeurons = 6
        )
        return object : LandOrganism {
            override val brain: Brain = brain
            override var health: Int = 100 // Start with 100 health
            override var energy: Int = 60  // Start with 50 energy
            override var age: Int = 0      // Start with age 0
            override val learningRate: Double = 0.02
            override val history: MutableMap<Int, String> = mutableMapOf()
        }
    }

}