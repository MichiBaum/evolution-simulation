package com.michibaum.evolutionsimulation.brain

class Connection(
    val from: Neuron, // Source neuron
    val to: Neuron, // Even a backward connection is possible
    var weight: Double // Weight of the connection
) {
}