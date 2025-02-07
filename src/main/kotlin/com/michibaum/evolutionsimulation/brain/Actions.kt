package com.michibaum.evolutionsimulation.brain

interface Action

class MoveAction : Action {
    override fun toString(): String = "MoveAction"
}

class EatAction : Action {
    override fun toString(): String = "EatAction"
}

class DangerFleeingAction : Action {
    override fun toString(): String = "DangerFleeingAction"
}