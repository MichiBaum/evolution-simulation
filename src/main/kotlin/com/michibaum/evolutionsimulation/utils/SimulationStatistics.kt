package com.michibaum.evolutionsimulation.utils

data class SimulationStatistics(
    val simulationId: Int,
    var ticks: Int = 0,
    var initialOrganisms: Int = 0,
    var finalOrganisms: Int = 0,
    var totalFoodConsumed: Int = 0,
    var averageEnergy: Double = 0.0
) {
    override fun toString(): String {
        return """
        Simulation $simulationId Statistics:
        -------------------------------------
        Total Ticks: $ticks
        Initial Organisms: $initialOrganisms
        Final Organisms: $finalOrganisms
        Total Food Consumed: $totalFoodConsumed
        Average Final Energy: ${"%.2f".format(averageEnergy)}
        Survival Rate: ${"%.2f".format(100.0 * finalOrganisms / initialOrganisms)}%
        """.trimIndent()
    }
}
