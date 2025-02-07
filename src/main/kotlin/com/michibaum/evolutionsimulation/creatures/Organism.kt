package com.michibaum.evolutionsimulation.creatures

import com.michibaum.evolutionsimulation.World
import com.michibaum.evolutionsimulation.brain.*
import com.michibaum.evolutionsimulation.food.Eatable
import com.michibaum.evolutionsimulation.landmass.EarthTile
import com.michibaum.evolutionsimulation.landmass.WaterTile

interface Organism {

    val brain: Brain

    var health: Int
    var energy: Int
    var age: Int
    val learningRate: Double // Learning rate for weight adjustment

    companion object {
        private val visionSense = VisionSense()
        private val smellSense = SmellSense()
        private val touchSense = TouchSense()
    }

    // Organism consumes energy and ages every tick
    fun tick() {
        energy -= 1 // Energy decreases each tick
        age += 1  // Age increases each tick

        // Handle health based on energy
        if (energy <= 0) {
            energy = 0
            health -= 1 // Health decreases when energy runs out
        }

        if (age % 100 == 0) {
            brain.pruneWeakConnections(0.1) // Prune weak connections
            brain.growRandomConnections(5) // Grow random new connections
        }

    }

    // Sense the world and return sensory input
    fun sense(world: World, x: Int, y: Int): Map<Sense, Double> {
        val currentTile = world.tiles[x][y]
        val senseData = mutableMapOf<Sense, Double>()

        // Sense tile type (e.g., Earth or Water)
        senseData[visionSense] = when (currentTile) {
            is EarthTile -> 1.0
            is WaterTile -> 0.0
            else -> 0.0
        }

        // Sense presence of food
        senseData[smellSense] = if (currentTile is EarthTile && currentTile.food != null) 1.0 else 0.0

        // Sense health level
        senseData[touchSense] = health.toDouble() / 100.0

        return senseData
    }

    // Perform actions and learn
    fun act(world: World, x: Int, y: Int): Double { // Return the cumulative reward for this cycle
        val sensoryData = sense(world, x, y)
        brain.processInput(sensoryData)

        val actions = brain.triggerActions()
        var totalReward = 0.0

        for (action in actions) {
            when (action) {
                is MovementAction -> {
                    val newPosition = determineMovement(world, x, y)
                    totalReward += rewardMovement(newPosition, world) // Add reward for movement
                }
                is FoodAction -> {
                    val currentTile = world.tiles[x][y]
                    if (currentTile is EarthTile && currentTile.food != null) {
                        eat(currentTile.food!!)
                        currentTile.food = null // Remove food after eating
                        totalReward += 1.0 // Reward for successfully eating
                    } else {
                        totalReward -= 10.0 // Penalty for trying to eat without food
                    }
                }
                is DangerAction -> {
                    totalReward += 0.5 // Reward for avoiding danger
                }
            }
        }

        // Teach the brain based on the accumulated reward
        brain.adjustWeightsBasedOnReward(totalReward, learningRate)

        return totalReward
    }

    // Determines movement based on environment and returns the new position
    fun determineMovement(world: World, x: Int, y: Int): Pair<Int, Int> {
        val possibleMoves = listOf(
            Pair(x - 1, y), // Up
            Pair(x + 1, y), // Down
            Pair(x, y - 1), // Left
            Pair(x, y + 1)  // Right
        )

        // Filter moves to include only valid (in-bounds) positions
        val validMoves = possibleMoves.filter { (newX, newY) ->
            newX in world.tiles.indices && newY in world.tiles[newX].indices
        }

        // Prioritize moves based on food presence
        val foodMoves = validMoves.filter { (newX, newY) ->
            world.tiles[newX][newY] is EarthTile &&
                    (world.tiles[newX][newY] as EarthTile).food != null
        }
        if (foodMoves.isNotEmpty()) {
            return foodMoves.first()
        }

        // Default: Random valid movement
        return validMoves.random()
    }

    // Reward movement based on the type of tile or proximity to food
    fun rewardMovement(newPosition: Pair<Int, Int>, world: World): Double {
        val tile = world.getTileAt(newPosition.first, newPosition.second)

        return when (tile) {
            is EarthTile -> 1.0 // Basic reward for moving to an EarthTile
            is WaterTile -> -1.0 // Penalize movement into WaterTile
            is Eatable -> 1.0 // Extra reward for moving closer to food
            else -> 0.0 // Neutral
        }
    }


    // Consume food and gain energy
    fun eat(eatable: Eatable) {
        energy += eatable.energy // Restore energy from food
    }

    // Find a nearby position within the world for placing the offspring
    private fun findNearbyPosition(world: World, currentPosition: Pair<Int, Int>): Pair<Int, Int>? {
        val (x, y) = currentPosition
        val possibleMoves = listOf(
            Pair(x - 1, y), // Up
            Pair(x + 1, y), // Down
            Pair(x, y - 1), // Left
            Pair(x, y + 1)  // Right
        )
        return possibleMoves.find { (newX, newY) ->
            newX in world.tiles.indices &&
                    newY in world.tiles[newX].indices &&
                    world.tiles[newX][newY] is EarthTile &&
                    world.tiles[newX][newY].food == null // Ensure no food occupies the tile
        }
    }

}