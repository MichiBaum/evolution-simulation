package com.michibaum.evolutionsimulation.utils

import com.michibaum.evolutionsimulation.brain.*
import com.michibaum.evolutionsimulation.brain.actions.*
import com.michibaum.evolutionsimulation.brain.senses.Sense
import com.michibaum.evolutionsimulation.brain.senses.allSenses
import kotlin.random.Random

class BrainGenerator {

    fun generateRandomBrain(
        numInterneurons: Int,
        numMotorNeurons: Int
    ): Brain {
        val senses = createSenses()
        val actions = createActions()

        // Create neurons
        val sensoryNeurons = (1..senses.size).map { Neuron(activationValue = NEURON_INITIAL_ACTIVATION_VALUE) } // Match the size of senses
        val interneurons = (1..numInterneurons).map { Neuron(activationValue = NEURON_INITIAL_ACTIVATION_VALUE) }
        val motorNeurons = (1..numMotorNeurons).map { Neuron(activationValue = NEURON_INITIAL_ACTIVATION_VALUE) }

        // Create random actions

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
            motorNeuronToActionMapping = motorNeuronToActionMapping
        )
    }
    fun createSenses() : List<Sense> = allSenses().map { it.invoke() }

    fun createActions(): List<Action> = listOf(
        { MoveAction(Direction.UP) },
        { MoveAction(Direction.DOWN) },
        { MoveAction(Direction.LEFT) },
        { MoveAction(Direction.RIGHT) },
        { EatAction() },
        { DangerFleeingAction() }
    ).map { it.invoke() }

    private fun connectNeuronsRandomly(fromNeurons: List<Neuron>, toNeurons: List<Neuron>, connectionProbability: Double = 0.2) {
        val maxWeight = 0.5
        val minWeight = -0.5
        fromNeurons.forEach { from ->
            toNeurons.forEach { to ->
                if (Random.nextDouble() < connectionProbability) { // Only connect based on probability
                    val weight = Random.nextDouble(minWeight, maxWeight)
                    val connection = Connection(from = from, to = to, weight = weight)
                    to.incomingConnections.add(connection)
                }
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