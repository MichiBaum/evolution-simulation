package com.michibaum.evolutionsimulation.brain

import kotlin.math.exp

class Neuron(
    val incomingConnections: MutableList<Connection> = mutableListOf(),
    var activationValue: Double = 0.0, // Output or state of the neuron
    private var memoryDecay: Double = 0.2, // Decay factor for memory
    private val activationFunction: (Double) -> Double = ::sigmoid // Allow dynamic activation function

) {
    private var previousActivationValue: Double = 0.0

    /**
     * Computes the activation value of a neuron based on its incoming connections and a specified activation function.
     *
     * The method calculates a weighted sum of the activation values from incoming connections,
     * applies the neuron-specific activation function, and incorporates a memory decay factor.
     * The memory decay blends the newly computed activation value with the previously stored value.
     *
     * Steps:
     * 1. Checks if the neuron has any incoming connections. If none are present, the computation is skipped.
     * 2. Calculates the weighted sum of incoming connection values using their respective weights.
     * 3. Applies the activation function to the weighted sum to determine the neuron’s activation value.
     * 4. Adjusts the activation value based on a memory decay factor, blending it with the prior activation value.
     * 5. Updates the previous activation value to reflect the current activation value for the next iteration.
     */
    fun computeActivation() {
        if (incomingConnections.isEmpty()) return

        val weightedSum = incomingConnections.sumOf {
            it.from.activationValue * it.weight
        }

        // Apply the selected activation function
        activationValue = activationFunction(weightedSum) * (1 - memoryDecay) + previousActivationValue * memoryDecay

        // Update previous activation value for memory decay
        previousActivationValue = activationValue
    }

    // Predefined activation functions
    companion object {
        /**
         * Computes the sigmoid activation function on the given input.
         * The sigmoid function is defined as 1 / (1 + exp(-x)), transforming
         * the input into a value within the range [0, 1].
         *
         * @param x The input value for which the sigmoid function will be computed.
         * @return The result of the sigmoid function applied to the input, ranging between 0 and 1.
         */
        fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))
    }

    /**
     * Adjusts the memory decay factor of a neuron based on the reward.
     *
     * @param reward The reward signal (positive for reinforcing, negative for punishing).
     */
    fun adjustMemoryDecayBasedOnReward(reward: Double) {
        memoryDecay = if (reward > 0) {
            (memoryDecay + 0.01).coerceAtMost(1.0) // Rewarded: retain more memory
        } else {
            (memoryDecay - 0.01).coerceAtLeast(0.5) // Punished: encourage faster forgetting
        }
    }



}