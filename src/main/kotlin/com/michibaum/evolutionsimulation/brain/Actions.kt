package com.michibaum.evolutionsimulation.brain

interface Action

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}
class MoveAction(val direction: Direction) : Action {
    override fun toString(): String = "MoveAction $direction"
}

class EatAction : Action {
    override fun toString(): String = "EatAction"
}

class DangerFleeingAction : Action {
    override fun toString(): String = "DangerFleeingAction"
}