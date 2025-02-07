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
    var organisms: MutableList<Organism>
){
    fun getTileAt(x: Int, y: Int): Tile {
        return tiles[x][y]
    }


}


class WorldGenerator(){

    fun generateRandomWorldWithOrganisms(width: Int, height: Int): World {
        val tiles: Array<Array<Tile>> = Array(width) {
            Array(height) {
                if (Math.random() < 0.7) EarthTile() else WaterTile()
            }
        }

        val organisms = mutableListOf<Organism>()

        // Add random land organisms
        val numberOfOrganisms = width * height / 4
        repeat(numberOfOrganisms) {
            val brain = BrainGenerator().generateRandomBrain(
                numSenses = 3,
                numActions = 3,
                numInterneurons = 10,
                numMotorNeurons = 3
            )
            organisms.add(object : LandOrganism {
                override val brain: Brain = brain
                override var health: Int = 100 // Start with 100 health
                override var energy: Int = 50  // Start with 50 energy
                override var age: Int = 0      // Start with age 0
                override val learningRate: Double = 0.1
            })
        }

        return World(tiles, organisms)
    }



}