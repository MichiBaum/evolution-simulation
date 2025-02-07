package com.michibaum.evolutionsimulation.brain

import kotlin.random.Random

class Brain(
    val sensoryNeurons: List<Neuron>,
    val interneurons: List<Neuron>,
    val motorNeurons: List<Neuron>,
    val senses: List<Sense>,
    val actions: List<Action>,
    val motorNeuronToActionMapping: Map<Neuron, Action>
) {
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


    fun triggerActions(): List<Action> {
        val activatedActions = mutableListOf<Action>()

        motorNeuronToActionMapping.forEach { (motorNeuron, action) ->
            if (motorNeuron.activationValue > 0.6) { // Threshold for action triggering
                activatedActions.add(action)
            }
        }

        return activatedActions
    }

    fun adjustWeightsBasedOnReward(reward: Double, learningRate: Double) {
        motorNeurons.forEach { motorNeuron ->
            motorNeuron.incomingConnections.forEach { connection ->
                // Standard reward-driven weight adjustment
                val deltaReward = reward * connection.from.activationValue * learningRate
                connection.weight += deltaReward

                // Hebbian learning (co-activation adjustment)
                val hebbianFactor = connection.from.activationValue * motorNeuron.activationValue
                connection.weight += learningRate * hebbianFactor
            }
        }
    }

    fun pruneWeakConnections(threshold: Double) {
        (sensoryNeurons + interneurons + motorNeurons).forEach { neuron ->
            neuron.incomingConnections.removeIf { connection ->
                Math.abs(connection.weight) < threshold // Prune low-weight connections
            }
        }
    }

    fun growRandomConnections(newConnections: Int) {
        repeat(newConnections) {
            val fromNeuron = (sensoryNeurons + interneurons).random()
            val toNeuron = (interneurons + motorNeurons).random()
            val weight = Random.nextDouble(-0.5, 0.5)

            val newConnection = Connection(from = fromNeuron, to = toNeuron, weight = weight)
            toNeuron.incomingConnections.add(newConnection)
        }
    }


}
