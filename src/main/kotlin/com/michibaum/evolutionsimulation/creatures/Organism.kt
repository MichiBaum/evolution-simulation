package com.michibaum.evolutionsimulation.creatures

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.brain.*
import com.michibaum.evolutionsimulation.brain.actions.DangerFleeingAction
import com.michibaum.evolutionsimulation.brain.actions.Direction
import com.michibaum.evolutionsimulation.brain.actions.EatAction
import com.michibaum.evolutionsimulation.brain.actions.MoveAction
import com.michibaum.evolutionsimulation.brain.senses.*
import com.michibaum.evolutionsimulation.food.Eatable
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.Tile
import com.michibaum.evolutionsimulation.landmass.WaterTile
import com.michibaum.evolutionsimulation.utils.realityKicksIn
import java.util.UUID

interface Organism {

    val history: MutableMap<Int, String>
    val id: UUID
    val brain: Brain

    val senses: List<Sense>

    var health: Int
    var energy: Int
    var age: Int
    val learningRate: Double // Learning rate for weight adjustment

    companion object {

    }

    fun isAlive(): Boolean = health > 0

    // Organism consumes energy and ages every tick
    fun timeGoesOn() {
        energy -= 1 // Energy decreases each tick
        age += 1  // Age increases each tick

        // Handle health based on energy
        if (energy <= 0) {
            energy = 0
            health -= 1 // Health decreases when energy runs out
        }

    }

    // Sense the world and return sensory input
    fun sense(world: World, currentTile: Tile): Map<Sense, Double> {
        val senseData = mutableMapOf<Sense, Double>()

        senses.forEach { sense ->
            senseData[sense] = sense.calc(world, organism = this, currentTile = currentTile)
        }

        return senseData
    }

    // Perform actions and learn
    fun act(world: World, currentTile: Tile, ticks: Int): Double { // Return the cumulative reward for this cycle
//        val actions = brain.triggerActions()
        val actions = listOfNotNull(brain.triggerSingleAction())

        var totalReward = 0.0

        var newMovePosition: Pair<Int, Int>? = null
        if (realityKicksIn(ticks)) {
            val organism = currentTile.organism
            history[ticks] =
                "Creature -> Health: ${organism?.health} Energy: ${organism?.energy} Actions: $actions      Tile has food: ${currentTile.hasFood()}"
        }

        for (action in actions) {
            when (action) {
                is MoveAction -> {
                    newMovePosition = determineMovement(world, currentTile.location_x, currentTile.location_y, action.direction)
                    totalReward += rewardMovement(newMovePosition, world) // Add reward for movement
                }

                is EatAction -> {
                    if (currentTile is EarthTile && currentTile.food != null) {
                        eat(currentTile.food!!)
                        currentTile.food = null // Remove food after eating
                        if (energy > 100)
                            totalReward -= 1.0
                        else
                            totalReward += 3.0 // Reward for successfully eating
                    } else {
                        totalReward -= 20.0 // Penalty for trying to eat without food
                    }
                }

                is DangerFleeingAction -> {
//                    totalReward += 0.5 // Reward for avoiding danger
                }
            }
        }

        timeGoesOn()

        if (realityKicksIn(ticks)){
            if (!isAlive()) {
                currentTile.organism = null
                return totalReward
            }
        }

        // Teach the brain based on the accumulated reward
        brain.adjustWeightsBasedOnReward(totalReward, learningRate)

        if (newMovePosition != null)
            moveCreature(world, currentTile, newMovePosition)

        if (ticks % 50 == 0) {
            val pruned = brain.pruneWeakConnections(0.2) // Prune weak connections
            brain.growRandomConnections(pruned) // Grow random new connections
        }

        return totalReward
    }

    fun moveCreature(world: World, currentTile: Tile, newPosition: Pair<Int, Int>) {
        currentTile.organism?.let {
            world.setOrganismAt(currentTile.location_x, currentTile.location_y, it)
            currentTile.organism = null
        }
    }

    // Determines movement based on environment and returns the new position
    fun determineMovement(world: World, x: Int, y: Int, direction: Direction): Pair<Int, Int> {
        val move = when (direction) {
            Direction.LEFT -> Pair(x, y - 1)
            Direction.RIGHT -> Pair(x, y + 1)
            Direction.UP -> Pair(x - 1, y)
            Direction.DOWN -> Pair(x + 1, y)
        }
        val xMoveValid = move.first in world.tiles.indices
        val newX = if (!xMoveValid)
            Math.floorMod(move.first, world.tiles.size)
        else
            move.first

        val yMoveValid = move.second in world.tiles[newX].indices
        val newY = if (!yMoveValid)
            Math.floorMod(move.second, world.tiles[newX].size)
        else
            move.second

        return Pair(newX, newY)
    }

    // Reward movement based on the type of tile or proximity to food
    fun rewardMovement(newPosition: Pair<Int, Int>, world: World): Double {
        val tile = world.getTileAt(newPosition.first, newPosition.second)

        var reward = 0.0

        if (tile.hasOrganisms()) {
            reward -= 1.0
        }

        if (tile is EarthTile) {
            reward += 0.2
            if (tile.food != null) {
                reward += 0.5
            }
        }

        if (tile is WaterTile) {
            reward -= 0.5
        }

        return reward
    }


    // Consume food and gain energy
    fun eat(eatable: Eatable) {
        energy += eatable.energy // Restore energy from food
    }

}