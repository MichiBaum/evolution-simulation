package com.michibaum.evolutionsimulation.brain

class Neuron(
    val incomingConnections: MutableList<Connection> = mutableListOf(),
    var activationValue: Double = 0.0, // Output or state of the neuron
    private var memoryDecay: Double = 0.9 // Decay factor for memory
) {
    var previousActivationValue: Double = 0.0 // Stores activation history for memory

    // Compute neuron activation with updated weight sums and memory effects
    fun computeActivation() {
        if (incomingConnections.isEmpty()) return

        val weightedSum = incomingConnections.sumOf {
            it.from.activationValue * it.weight
        }

        // Choose dynamic activation function (sigmoid, ReLU, tanh, etc.)
        activationValue = sigmoid(weightedSum) * (1 - memoryDecay) + previousActivationValue * memoryDecay

        // Update previous activation value for memory decay
        previousActivationValue = activationValue
    }

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + Math.exp(-x))

    private fun tanh(x: Double): Double = Math.tanh(x)

    private fun relu(x: Double): Double = if (x > 0) x else 0.0

    // Adjust neuron memory decay dynamically (reward, stimulation, etc.)
    fun adjustMemoryDecay(increase: Boolean) {
        memoryDecay = if (increase) {
            (memoryDecay + 0.05).coerceAtMost(1.0) // Limit to max 1.0
        } else {
            (memoryDecay - 0.05).coerceAtLeast(0.5) // Avoid 0 or negative decay
        }
    }

}