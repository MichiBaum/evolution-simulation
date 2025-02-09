package com.michibaum.evolutionsimulation.utils

const val WORLD_SIZE_X: Int = 100
const val WORLD_SIZE_Y: Int = 100

const val VEGETABLE_SPAWN_CHANCE: Double = 0.1 // 10% probability

const val MOTOR_NEURON_ACTIVATION_THRESHOLD: Double = 0.1
const val CONNECTION_INIT_WEIGHT_MIN: Double = -0.5
const val CONNECTION_INIT_WEIGHT_MAX: Double = 0.5
const val CONNECTION_WEIGHT_MIN: Double = -5.0
const val CONNECTION_WEIGHT_MAX: Double = 5.0
const val BRAIN_NEURON_LEARN_DEPTH: Int = 8

const val NEURON_MEMORY_DECAY: Double = 0.01
const val NEURON_MEMORY_DECAY_INCREASE: Double = 0.01
const val NEURON_NEURON_MEMORY_DECAY_MAX: Double = 1.0
const val NEURON_INITIAL_ACTIVATION_VALUE: Double = 0.2

const val ORGANISM_INIT_ENERGY: Int = 60
const val ORGANISM_INIT_HEALTH: Int = 100
const val ORGANISM_MAX_AGE: Int = 100
const val ORGANISM_LEARNING_RATE: Double = 0.02

const val FOOD_SPAWN_AFTER_TICKS: Int = 30

const val SIMULATIONS: Int = 1
const val REALITY_AFTER_TICKS: Int = 200000
const val SIMULATION_MAX_TICKS: Int = REALITY_AFTER_TICKS + 500
fun realityKicksIn(ticks: Int) = REALITY_AFTER_TICKS < ticks