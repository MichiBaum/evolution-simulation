package com.michibaum.evolutionsimulation.brain

import com.michibaum.evolutionsimulation.utils.CONNECTION_INIT_WEIGHT_MAX
import com.michibaum.evolutionsimulation.utils.CONNECTION_INIT_WEIGHT_MIN
import com.michibaum.evolutionsimulation.utils.MOTOR_NEURON_ACTIVATION_THRESHOLD
import kotlin.math.abs
import kotlin.random.Random

class Brain(
    val sensoryNeurons: List<Neuron>,
    val interneurons: List<Neuron>,
    val motorNeurons: List<Neuron>,
    val senses: List<Sense>,
    val motorNeuronToActionMapping: Map<Neuron, Action>
) {

    /**
     * Processes sensory input by setting the activation values for sensory neurons, propagating
     * these values through interneurons and motor neurons.
     *
     * This method updates the state of the sensory neurons based on the provided sensory data,
     * computes activation for interneurons to validate their response to the sensory input,
     * and ensures that activation values propagate through to motor neurons.
     *
     * @param sensoryData A map where keys represent senses (e.g., vision, hearing, etc.) and values represent
     *                    the corresponding intensity of the sensory input as a double value. If a sense is
     *                    not included in the map, its activation value is set to 0.0.
     */
    fun processInput(sensoryData: Map<Sense, Double>) {
        // Step 1: Set sensory activation values
        sensoryNeurons.forEachIndexed { index, neuron ->
            neuron.activationValue = sensoryData[senses[index]] ?: 0.0
        }

        // Step 2: Validate that inputs influence interneurons
        interneurons.forEachIndexed { index, neuron ->
            neuron.computeActivation()
        }

        // Step 3: Validate that signals propagate to motor neurons
        motorNeurons.forEachIndexed { index, neuron ->
            neuron.computeActivation()
        }
    }


    /**
     * Identifies actions to be triggered based on the activation values of motor neurons.
     *
     * This method evaluates the activation values of motor neurons mapped to specific actions.
     * If a motor neuron's activation value exceeds a predefined threshold, the corresponding
     * action is included in the list of triggered actions.
     *
     * @return A list of actions that are triggered based on the activation states of the motor neurons.
     */
    fun triggerActions(): List<Action> {
        val activatedActions = mutableListOf<Action>()

        motorNeuronToActionMapping.forEach { (motorNeuron, action) ->
            if (motorNeuron.activationValue > MOTOR_NEURON_ACTIVATION_THRESHOLD) { // Threshold for action triggering
                activatedActions.add(action)
            }
        }

        return activatedActions
    }

    /**
     * Modifies the weights of the connections in motor neurons based on the provided reward and learning rate.
     *
     * This method adjusts the weights of the incoming connections for each motor neuron in the neural network
     * using a reward-based learning mechanism. The adjustment includes both a direct reward influence and
     * a Hebbian learning factor, which reinforces connections based on the co-activation of neurons.
     * Additionally, it dynamically adjusts the memory decay factor of motor neurons depending on
     * whether the reward is positive or negative.
     *
     * @param reward The reward signal used to adjust the weights. A positive reward strengthens connections,
     *               while a negative reward weakens them.
     * @param learningRate The learning rate that scales the magnitude of weight adjustments. Higher values
     *                     result in larger updates to the connection weights.
     */
    fun adjustWeightsBasedOnReward(reward: Double, learningRate: Double) {
        motorNeurons.forEach { motorNeuron ->
            motorNeuron.incomingConnections.forEach { connection ->
                val deltaReward = reward * connection.from.activationValue * learningRate
                connection.weight += deltaReward

                val hebbianFactor = connection.from.activationValue * motorNeuron.activationValue
                connection.weight += learningRate * hebbianFactor
            }

            // Adjust decay dynamically based on reward feedback
            motorNeuron.adjustMemoryDecay(increase = reward > 0)
        }
    }

    /**
     * Removes weak connections from the neurons in the brain whose weights are below the specified threshold.
     *
     * This method iterates through all neurons (sensory neurons, interneurons, and motor neurons) in the brain
     * and removes any incoming connections with absolute weights less than the provided threshold. The pruning
     * of weak connections helps in optimizing the neural network by eliminating insignificant connections.
     *
     * @param threshold The minimum absolute value a connection's weight must have to remain in the network.
     *                  Connections with weights below this threshold are removed.
     */
    fun pruneWeakConnections(threshold: Double) {
        (sensoryNeurons + interneurons + motorNeurons).forEach { neuron ->
            neuron.incomingConnections.removeIf { connection ->
                abs(connection.weight) < threshold // Prune low-weight connections
            }
        }
    }

    /**
     * Adds a specified number of random connections between neurons within the brain.
     *
     * This method randomly selects neurons from the sensory and interneuron groups as source neurons
     * and randomly selects neurons from the interneuron and motor neuron groups as target neurons to form new connections.
     * The weight for each new connection is assigned a random value within a predefined range.
     *
     * @param newConnections The number of new random connections to be created.
     */
    fun growRandomConnections(newConnections: Int) {
        repeat(newConnections) {
            val fromNeuron = (sensoryNeurons + interneurons).random()
            val toNeuron = (interneurons + motorNeurons).random()
            val weight = Random.nextDouble(CONNECTION_INIT_WEIGHT_MIN, CONNECTION_INIT_WEIGHT_MAX)

            val newConnection = Connection(from = fromNeuron, to = toNeuron, weight = weight)
            toNeuron.incomingConnections.add(newConnection)
        }
    }


}
