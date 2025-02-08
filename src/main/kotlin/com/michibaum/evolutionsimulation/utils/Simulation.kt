package com.michibaum.evolutionsimulation.utils

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.WorldGenerator
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.food.Vegetable
import kotlin.random.Random

class Simulation {

    private val world: World = WorldGenerator().generateRandomWorldWithOrganisms(width = WORLD_SIZE_X, height = WORLD_SIZE_Y)
    private val initialOrganisms = world.getAllOrganisms().size

    var greatResetHappened = false

    // Function for running a single simulation
    fun runSimulation(simulationId: Int, maxTicks: Int): SimulationStatistics {
        var ticks = 0

        // Simulate for a given number of ticks or until all organisms are gone
        while (ticks < maxTicks && world.getAllOrganisms().isNotEmpty()) {
            if (realityKicksIn(ticks) && !greatResetHappened){
                world.getAllOrganisms().forEach {
                    it.energy = 60
                    it.health = 100
                    it.age = 0
                }
                greatResetHappened = true
            }
            if (!realityKicksIn(ticks) && ticks % 150 == 0){
                world.getAllOrganisms().forEach {
                    it.energy = 60
                    it.health = 100
                    it.age = 0
                }
            }
            println("Simulation $simulationId tick $ticks, organisms: ${world.getAllOrganisms().size}")
            simulate(world, ticks)
            ticks++
        }

        for (organism in world.getAllOrganisms()) {
            println("Organism $simulationId: $organism")
            visualizeBrain(organism)
            organism.history.forEach { println(it) }
        }

        // Collect and return statistics
        return SimulationStatistics(
            simulationId = simulationId,
            ticks = ticks,
            initialOrganisms = initialOrganisms,
            finalOrganisms = world.getAllOrganisms().size,
            totalFoodConsumed = trackFoodConsumption(world),
            averageEnergy = calculateAverageEnergy(world)
        )
    }

    fun visualizeBrain(organism: Organism) {
        val visualizer = BrainGraphVisualizer()
        val dotGraph = visualizer.visualizeBrainGraph(organism.brain)
        println(dotGraph)
    }

    // Simulate a single step (tick) in the world
    fun simulate(world: World, ticks: Int) {
        removeAllFood(world)
        simulateFoodSpawns(world)

        world.getTilesWithOrganisms().forEach { tile ->
            val organism = tile.organism!!
            val sensoryInput = organism.sense(world, tile)
            organism.brain.processInput(sensoryInput)
            organism.act(world, tile, ticks)
        }
    }

    private fun removeAllFood(world: World) {
        world.tiles.forEach { row ->
            row.forEach { tile ->
                tile.food = null
            }
        }
    }

    // Calculate food consumption
    fun trackFoodConsumption(world: World): Int {
        return world.getAllOrganisms().sumOf { organism ->
            organism.energy
        }
    }

    // Calculate average energy of remaining organisms
    fun calculateAverageEnergy(world: World): Double {
        if (world.getAllOrganisms().isEmpty()) return 0.0
        return world.getAllOrganisms().sumOf { it.energy.toDouble() } / world.getAllOrganisms().size
    }

    // Simulate spawning of food across the world
    fun simulateFoodSpawns(world: World) {
        world.tiles.forEach { row ->
            row.forEach { tile ->
                if (!tile.hasFood() && Random.nextDouble() < VEGETABLE_SPAWN_CHANCE) {
                    tile.food = Vegetable()
                }
            }
        }
    }

}