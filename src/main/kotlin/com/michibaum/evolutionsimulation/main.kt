package com.michibaum.evolutionsimulation

import com.michibaum.evolutionsimulation.utils.SIMULATIONS
import com.michibaum.evolutionsimulation.utils.SIMULATION_MAX_TICKS
import com.michibaum.evolutionsimulation.utils.Simulation
import com.michibaum.evolutionsimulation.utils.SimulationStatistics
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

// Entry point for the program
fun main() = runBlocking {

    println("Starting $SIMULATIONS simulations, each running for up to $SIMULATION_MAX_TICKS ticks...")

    // Run simulations concurrently
    val simulationResults = (1..SIMULATIONS).map { simulationId ->
        async {
            Simulation().runSimulation(simulationId, SIMULATION_MAX_TICKS)
        }
    }.awaitAll()

    println("All $SIMULATIONS simulations completed.")

    // Print statistics for each simulation
    simulationResults.forEach { stats ->
        println(stats)
    }

    // Summarize overall statistics
    summarizeOverallStatistics(simulationResults)
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
