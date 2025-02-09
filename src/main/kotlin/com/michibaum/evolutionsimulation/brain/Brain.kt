package com.michibaum.evolutionsimulation.brain

import com.michibaum.evolutionsimulation.utils.*
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
        // Ensure sensory neurons correctly receive input
        sensoryNeurons.forEach { neuron ->
            neuron.activationValue = sensoryData[senses[sensoryNeurons.indexOf(neuron)]] ?: 0.0
        }

        // Propagate activation through the network
        interneurons.forEach { it.computeActivation() }
        motorNeurons.forEach { it.computeActivation() }

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

    fun triggerSingleAction(): Action? {
        return motorNeuronToActionMapping
            .maxByOrNull { (neuron, _) -> neuron.activationValue }
            ?.takeIf { it.key.activationValue > MOTOR_NEURON_ACTIVATION_THRESHOLD } // Only consider if activation exceeds threshold
            ?.value
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
        if (reward == 0.0) return
        motorNeurons.forEach { motorNeuron ->
            // Directly adjust motor neuron weights
            motorNeuron.incomingConnections.forEach { connection ->
                val error = reward - motorNeuron.activationValue
                val weightUpdate = learningRate * error * connection.from.activationValue
                connection.weight = (connection.weight + weightUpdate)
                    .coerceIn(CONNECTION_WEIGHT_MIN, CONNECTION_WEIGHT_MAX)
            }

            // Propagate reward to earlier neurons
            propagateWeightAdjustment(motorNeuron, reward, learningRate, depth = 1)
        }

    }

    /**
     * Recursively propagate the reward signal backward through the network.
     *
     * @param neuron The current neuron being updated.
     * @param reward The reward signal affecting this neuron.
     * @param learningRate The adjustment magnitude for weight updates.
     * @param depth The current recursion depth; affects reward decay for deeper neurons.
     */
    private fun propagateWeightAdjustment(
        neuron: Neuron,
        reward: Double,
        learningRate: Double,
        depth: Int
    ) {
        // Update neuron
        neuron.adjustMemoryDecayBasedOnReward(reward)

        // Iterate through every incoming connection to adjust weights
        neuron.incomingConnections.forEach { connection ->
            val sourceNeuron = connection.from

            // Reward scaling based on depth (closer neurons get higher reward)
            val scaledReward = reward / (depth + 1 / 2.0)

            // Hebbian learning: strengthen connections based on co-activation
            val activationContribution = sourceNeuron.activationValue

            // Update the connection weight
            val weightUpdate = learningRate * scaledReward * activationContribution
            connection.weight += weightUpdate

            // Recursively propagate to the next level (if depth limit isn't reached)
            if (depth < BRAIN_NEURON_LEARN_DEPTH) { // Limit depth to avoid excessive recursion in large networks
                propagateWeightAdjustment(sourceNeuron, scaledReward, learningRate, depth + 1)
            }
        }
    }


    /**
     * Removes weak connections from the neurons in the brain based on a specified threshold.
     *
     * A weak connection is defined as having an absolute weight below the given threshold.
     * However, this method ensures that negative weights below the threshold are NOT removed
     * unless they are very close to zero to preserve inhibitory effects in the neural network.
     *
     * @param threshold The minimum absolute value a connection's weight must have to remain in the network.
     *                  Connections with weights below this threshold are considered weak and are removed.
     */
    fun pruneWeakConnections(threshold: Double): Int {
        var prunedCount = 0 // Counter for pruned connections

        (sensoryNeurons + interneurons + motorNeurons).forEach { neuron ->
            // Prune incoming connections and count the removed ones
            val initialSize = neuron.incomingConnections.size
            neuron.incomingConnections.removeIf { connection ->
                connection.weight > -threshold && connection.weight < threshold
            }
            val prunedFromNeuron = initialSize - neuron.incomingConnections.size
            prunedCount += prunedFromNeuron
        }

        return prunedCount // Return the total number of pruned connections
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
