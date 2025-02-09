package com.michibaum.evolutionsimulation.brain

import com.michibaum.evolutionsimulation.utils.NEURON_MEMORY_DECAY
import com.michibaum.evolutionsimulation.utils.NEURON_MEMORY_DECAY_INCREASE
import com.michibaum.evolutionsimulation.utils.NEURON_NEURON_MEMORY_DECAY_MAX
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

class Neuron(
    val incomingConnections: MutableList<Connection> = mutableListOf(),
    var activationValue: Double,
    private var memoryDecay: Double = NEURON_MEMORY_DECAY,
    private val activationFunction: (Double) -> Double = ::gelu

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

        /**
         * Computes the ReLU (Rectified Linear Unit) activation function on the given input.
         * The ReLU function is defined as max(0, x), converting negative values to 0
         * and leaving non-negative values unchanged.
         *
         * @param x The input value for which the ReLU function will be computed.
         * @return The result of the ReLU function applied to the input, never below 0.
         */
        fun relu(x: Double): Double = if (x > 0) x else 0.0

        /**
         * Computes the Leaky ReLU activation function on the given input.
         * The Leaky ReLU function is defined as:
         *  - x if x >= 0
         *  - alpha * x if x < 0
         *
         * @param x The input value for which the Leaky ReLU function will be computed.
         * @param alpha The slope for negative x values, typically a small number like 0.01.
         * @return The result of applying the Leaky ReLU function to the input.
         */
        fun leakyRelu(x: Double, alpha: Double = 0.01): Double = if (x >= 0) x else alpha * x

        /**
         * Computes the GELU (Gaussian Error Linear Unit) activation function on the given input.
         * Uses a tanh-based approximation for the error function:
         * GELU(x) = 0.5 * x * (1 + tanh( sqrt(2 / π) * (x + 0.044715 * x^3 ) ) ).
         *
         * @param x The input value for which the GELU function will be computed.
         * @return The result of applying the GELU function to the input.
         */

        fun gelu(x: Double): Double = 0.5 * x * (1.0 + tanh(sqrt(2.0 / Math.PI) * (x + 0.044715 * x.pow(3.0))))

    }

    fun adjustMemoryDecayBasedOnReward(reward: Double) {
        memoryDecay = if (reward > 0) {
            (memoryDecay + NEURON_MEMORY_DECAY_INCREASE).coerceAtMost(NEURON_NEURON_MEMORY_DECAY_MAX) // Rewarded: retain more memory
        } else {
            (memoryDecay - NEURON_MEMORY_DECAY_INCREASE).coerceAtLeast(NEURON_NEURON_MEMORY_DECAY_MAX) // Punished: encourage faster forgetting
        }
    }

}