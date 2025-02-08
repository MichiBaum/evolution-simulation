package com.michibaum.evolutionsimulation.creatures

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.brain.*
import com.michibaum.evolutionsimulation.food.Eatable
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.Tile
import com.michibaum.evolutionsimulation.landmass.WaterTile
import com.michibaum.evolutionsimulation.utils.realityKicksIn

interface Organism {

    val history: MutableMap<Int, String>
    val brain: Brain

    var health: Int
    var energy: Int
    var age: Int
    val learningRate: Double // Learning rate for weight adjustment

    companion object {
        private val visionSenseUp = VisionSense(Direction.UP)
        private val visionSenseDown = VisionSense(Direction.DOWN)
        private val visionSenseLeft = VisionSense(Direction.LEFT)
        private val visionSenseRight = VisionSense(Direction.RIGHT)
        private val smellSense = SmellSense(null)
        private val smellSenseRight = SmellSense(Direction.RIGHT)
        private val smellSenseUp = SmellSense(Direction.UP)
        private val smellSenseDown = SmellSense(Direction.DOWN)
        private val smellSenseLeft = SmellSense(Direction.LEFT)
        private val touchSense = TouchSense()
        private val hungerSense = HungerSense()
        private val healthSense = HealthSense()
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

        // Sense tile type (e.g., Earth or Water)

        // Helper function to wrap coordinates
        fun wrapCoordinate(coord: Int, max: Int): Int = (coord + max) % max

        val maxX = world.tiles.size  // Width of the world
        val maxY = world.tiles[0].size  // Height of the world

        // Sense tile type (e.g., Earth or Water)
        for (direction in listOf(visionSenseUp, visionSenseDown, visionSenseLeft, visionSenseRight)) {
            when (direction.direction) {
                Direction.LEFT -> {
                    val wrappedX = wrapCoordinate(currentTile.location_x - 1, maxX)
                    senseData[direction] = if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
                }
                Direction.RIGHT -> {
                    val wrappedX = wrapCoordinate(currentTile.location_x + 1, maxX)
                    senseData[direction] = if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
                }
                Direction.UP -> {
                    val wrappedY = wrapCoordinate(currentTile.location_y - 1, maxY)
                    senseData[direction] = if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
                }
                Direction.DOWN -> {
                    val wrappedY = wrapCoordinate(currentTile.location_y + 1, maxY)
                    senseData[direction] = if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
                }
            }
        }


        // Sense presence of food
        for (direction in listOf(smellSense, smellSenseUp, smellSenseDown, smellSenseLeft, smellSenseRight)) {
            when (direction.direction) {
                Direction.LEFT -> {
                    val wrappedX = wrapCoordinate(currentTile.location_x - 1, maxX)
                    senseData[direction] = if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
                }
                Direction.RIGHT -> {
                    val wrappedX = wrapCoordinate(currentTile.location_x + 1, maxX)
                    senseData[direction] = if (world.tiles[wrappedX][currentTile.location_y] is EarthTile) 1.0 else 0.0
                }
                Direction.UP -> {
                    val wrappedY = wrapCoordinate(currentTile.location_y - 1, maxY)
                    senseData[direction] = if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
                }
                Direction.DOWN -> {
                    val wrappedY = wrapCoordinate(currentTile.location_y + 1, maxY)
                    senseData[direction] = if (world.tiles[currentTile.location_x][wrappedY] is EarthTile) 1.0 else 0.0
                }
                null -> senseData[direction] = if (currentTile is EarthTile && currentTile.food != null) 1.0 else 0.0
            }
        }

        // Sense hunger
        senseData[hungerSense] = if (energy < 60) -20.0 else 1.0

        // Sense health
        senseData[healthSense] = calculateHealthSense()

        return senseData
    }

    fun calculateHealthSense(): Double{
        return when {
            energy > 60 -> 1.5
            energy > 20 -> 1.0
            health <= 0 -> -10.0
            else -> 0.0
        }
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

        // Teach the brain based on the accumulated reward
        brain.adjustWeightsBasedOnReward(totalReward, learningRate)

        if (newMovePosition != null)
            moveCreature(world, currentTile, newMovePosition)

        timeGoesOn()

        if (realityKicksIn(ticks)){
            if (!isAlive()) {
                if (newMovePosition != null) {
                    world.setOrganismAt(newMovePosition.first, newMovePosition.second, null)
                } else {
                    currentTile.organism = null
                }
            }
        }

        if (ticks % 50 == 0) {
            val pruned = brain.pruneWeakConnections(0.2) // Prune weak connections
            brain.growRandomConnections(pruned) // Grow random new connections
        }

        return totalReward
    }

    fun moveCreature(world: World, currentTile: Tile, newPosition: Pair<Int, Int>) {
        val newTile = world.tiles[newPosition.first][newPosition.second]
        newTile.organism = currentTile.organism
        currentTile.organism = null
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