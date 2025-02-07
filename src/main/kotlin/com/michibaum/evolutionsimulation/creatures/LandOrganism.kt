package com.michibaum.evolutionsimulation.creatures

interface LandOrganism: Organism {
    override var health: Int
    override var energy: Int
    override var age: Int

}