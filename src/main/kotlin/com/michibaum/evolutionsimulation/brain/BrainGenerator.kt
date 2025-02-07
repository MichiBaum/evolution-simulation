package com.michibaum.evolutionsimulation.brain

import kotlin.random.Random

class BrainGenerator {

    fun generateRandomBrain(
        numSenses: Int,
        numActions: Int,
        numInterneurons: Int,
        numMotorNeurons: Int
    ): Brain {
        // Create random senses
        val senses = generateRandomSenses(numSenses)

        // Create neurons
        val sensoryNeurons = (1..numSenses).map { Neuron() } // Match the size of senses
        val interneurons = (1..numInterneurons).map { Neuron() }
        val motorNeurons = (1..numMotorNeurons).map { Neuron() }

        // Create random actions
        val actions = generateRandomActions(numActions)

        // Randomly connect the neurons with weighted connections
        connectNeuronsRandomly(sensoryNeurons, interneurons)
        connectNeuronsRandomly(interneurons, motorNeurons)

        // Map motor neurons to random actions
        val motorNeuronToActionMapping = createMotorNeuronActionMapping(motorNeurons, actions)

        return Brain(
            sensoryNeurons = sensoryNeurons,
            interneurons = interneurons,
            motorNeurons = motorNeurons,
            senses = senses,
            actions = actions,
            motorNeuronToActionMapping = motorNeuronToActionMapping
        )
    }

    private fun generateRandomSenses(numSenses: Int): List<Sense> {
        val senseTypes = listOf(
            { VisionSense() },
            { HearingSense() },
            { SmellSense() },
            { TasteSense() },
            { TouchSense() }
        )
        return List(numSenses) { senseTypes.random().invoke() }
    }

    private fun generateRandomActions(numActions: Int): List<Action> {
        val actionTypes = listOf(
            { MovementAction() },
            { FoodAction() },
            { DangerAction() }
        )
        return List(numActions) { actionTypes.random().invoke() }
    }

    private fun connectNeuronsRandomly(fromNeurons: List<Neuron>, toNeurons: List<Neuron>) {
        val maxWeight = 0.5 // Lower the maximum weight to reduce random influence
        val minWeight = -0.5
        fromNeurons.forEach { from ->
            toNeurons.forEach { to ->
                val weight = Random.nextDouble(minWeight, maxWeight)
                val connection = Connection(from = from, to = to, weight = weight)
                to.incomingConnections.add(connection)
            }
        }
    }

    private fun createMotorNeuronActionMapping(
        motorNeurons: List<Neuron>,
        actions: List<Action>
    ): Map<Neuron, Action> {
        val mapping = motorNeurons.associateWith { actions.random() } // Randomly assign actions
        return mapping
    }


}