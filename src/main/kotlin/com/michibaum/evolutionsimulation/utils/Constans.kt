package com.michibaum.evolutionsimulation.utils

const val WORLD_SIZE_X: Int = 50
const val WORLD_SIZE_Y: Int = 50

const val VEGETABLE_SPAWN_CHANCE: Double = 0.2 // 20% probability

const val MOTOR_NEURON_ACTIVATION_THRESHOLD: Double = 0.1
const val CONNECTION_INIT_WEIGHT_MIN: Double = -0.5
const val CONNECTION_INIT_WEIGHT_MAX: Double = 0.5
const val CONNECTION_WEIGHT_MIN: Double = -5.0
const val CONNECTION_WEIGHT_MAX: Double = 5.0

const val SIMULATIONS: Int = 1
const val REALITY_AFTER_TICKS: Int = 100000
const val SIMULATION_MAX_TICKS: Int = REALITY_AFTER_TICKS + 200
fun realityKicksIn(ticks: Int) = REALITY_AFTER_TICKS < ticks