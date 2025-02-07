package com.michibaum.evolutionsimulation

import com.michibaum.evolutionsimulation.brain.BrainGraphVisualizer
import com.michibaum.evolutionsimulation.brain.DangerAction
import com.michibaum.evolutionsimulation.brain.FoodAction
import com.michibaum.evolutionsimulation.brain.MovementAction
import com.michibaum.evolutionsimulation.creatures.LandOrganism
import com.michibaum.evolutionsimulation.creatures.Organism
import com.michibaum.evolutionsimulation.creatures.WaterOrganism
import com.michibaum.evolutionsimulation.food.Vegetable
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.WaterTile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

// Entry point for the program
fun main() = runBlocking {
    val numSimulations = 10
    val maxTicks = 1000

    println("Starting $numSimulations simulations, each running for up to $maxTicks ticks...")

    // Run simulations concurrently
    val simulationResults = (1..numSimulations).map { simulationId ->
        async { runSimulation(simulationId, maxTicks) }
    }.awaitAll()

    println("All $numSimulations simulations completed.")

    // Print statistics for each simulation
    simulationResults.forEach { stats ->
        println(stats)
    }

    // Summarize overall statistics
    summarizeOverallStatistics(simulationResults)
}

// Function for running a single simulation
suspend fun runSimulation(simulationId: Int, maxTicks: Int): SimulationStatistics {
    val world = WorldGenerator().generateRandomWorldWithOrganisms(width = 16, height = 16)
    val initialOrganisms = world.organisms.size
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

// Summarize statistics across multiple simulations
fun summarizeOverallStatistics(simulationResults: List<SimulationStatistics>) {
    val totalTicks = simulationResults.sumOf { it.ticks }
    val totalInitialOrganisms = simulationResults.sumOf { it.initialOrganisms }
    val totalFinalOrganisms = simulationResults.sumOf { it.finalOrganisms }
    val totalFoodConsumed = simulationResults.sumOf { it.totalFoodConsumed }
    val averageEnergyAcrossSimulations = simulationResults.map { it.averageEnergy }.average()

    println("\nOverall Simulation Statistics:")
    println("-------------------------------------")
    println("Total Simulations: ${simulationResults.size}")
    println("Total Ticks Simulated: $totalTicks")
    println("Average Initial Organisms: ${totalInitialOrganisms / simulationResults.size}")
    println("Average Final Organisms: ${totalFinalOrganisms / simulationResults.size}")
    println("Total Food Consumed: $totalFoodConsumed")
    println("Average Energy Across Simulations: ${"%.2f".format(averageEnergyAcrossSimulations)}")
}
