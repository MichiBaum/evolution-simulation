package com.michibaum.evolutionsimulation.utils

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.WorldGenerator
import com.michibaum.evolutionsimulation.creatures.LandOrganism
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.creatures.WaterOrganism
import com.michibaum.evolutionsimulation.food.Vegetable
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.WaterTile
import kotlin.random.Random

class Simulation {

    private val world: World = WorldGenerator().generateRandomWorldWithOrganisms(width = WORLD_SIZE_X, height = WORLD_SIZE_Y)
    private val initialOrganisms = world.organisms.size

    init {
    }

    // Function for running a single simulation
    suspend fun runSimulation(simulationId: Int, maxTicks: Int): SimulationStatistics {
        var ticks = 0

        // Simulate for a given number of ticks or until all organisms are gone
        while (ticks < maxTicks && world.organisms.isNotEmpty()) {
            simulate(world)
            ticks++
        }

        for (organism in world.organisms) {
            visualizeBrain(organism)
        }

        // Collect and return statistics
        return SimulationStatistics(
            simulationId = simulationId,
            ticks = ticks,
            initialOrganisms = initialOrganisms,
            finalOrganisms = world.organisms.size,
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
    fun simulate(world: World) {
        // Process each organism's logic
        world.organisms = world.organisms.filter { organism ->
            // Each organism performs its lifecycle actions (sense, act, and interact)
            val position = initializeOrganismPositions(world)[organism.hashCode()]
            if (position != null) {
                val (x, y) = position
                val sensoryInput = organism.sense(world, x, y)
                organism.brain.processInput(sensoryInput)
                val reward = organism.act(world, x, y)
                organism.brain.adjustWeightsBasedOnReward(reward, organism.learningRate)
            }

            // Organism ages, consumes energy, and dies if health <= 0
            organism.tick()
            organism.health > 0
        }.toMutableList()

        // Simulate food spawning
        simulateFoodSpawns(world)
    }

    // Calculate food consumption
    fun trackFoodConsumption(world: World): Int {
        return world.organisms.sumOf { organism ->
            organism.energy
        }
    }

    // Calculate average energy of remaining organisms
    fun calculateAverageEnergy(world: World): Double {
        if (world.organisms.isEmpty()) return 0.0
        return world.organisms.sumOf { it.energy.toDouble() } / world.organisms.size
    }

    // Generate initial organism positions
    fun initializeOrganismPositions(world: World): MutableMap<Int, Pair<Int, Int>> {
        val positions = mutableMapOf<Int, Pair<Int, Int>>()

        // Generate random positions for organisms, ensuring they are valid
        world.organisms.forEach { organism ->
            var validPosition: Pair<Int, Int>
            do {
                val x = Random.nextInt(world.tiles.size)
                val y = Random.nextInt(world.tiles[0].size)
                validPosition = x to y
            } while (!isValidPosition(world, validPosition.first, validPosition.second, organism))

            positions[organism.hashCode()] = validPosition
        }

        return positions
    }

    // Ensure an organism can be placed on a specific tile
    fun isValidPosition(world: World, x: Int, y: Int, organism: Organism): Boolean {
        val tile = world.tiles[x][y]

        // Example logic: Ensure organisms only land on EarthTiles (not WaterTiles)
        return when (organism) {
            is LandOrganism -> tile is EarthTile
            is WaterOrganism -> tile is WaterTile
            else -> false
        }
    }

    // Simulate spawning of food across the world
    fun simulateFoodSpawns(world: World) {
        world.tiles.forEach { row ->
            row.forEach { tile ->
                if (!tile.hasFood() && Random.nextDouble() < 0.1) {
                    tile.food = Vegetable() // Spawn vegetables with 10% probability
                }
            }
        }
    }

}