package com.michibaum.evolutionsimulation.utils

import com.michibaum.evolutionsimulation.brain.Brain
import com.michibaum.evolutionsimulation.brain.Neuron
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.dot.DOTExporter
import org.jgrapht.nio.DefaultAttribute
import java.io.StringWriter


class BrainGraphVisualizer {
    fun visualizeBrainGraph(brain: Brain): String {
        // Create the graph
        val graph = DefaultDirectedWeightedGraph<Neuron, DefaultWeightedEdge>(DefaultWeightedEdge::class.java)

        val sensesToNeurons = brain.sensoryNeurons.zip(brain.senses).toMap()
        val motorNeuronsToActions = brain.motorNeuronToActionMapping

        // Add all neurons as nodes
        (brain.sensoryNeurons + brain.interneurons + brain.motorNeurons).forEach { neuron ->
            graph.addVertex(neuron)
        }

        // Add connections as weighted edges
        (brain.sensoryNeurons + brain.interneurons + brain.motorNeurons).forEach { neuron ->
            neuron.incomingConnections.forEach { connection ->
                val edge = graph.addEdge(connection.from, neuron)
                if (edge != null) {
                    graph.setEdgeWeight(edge, connection.weight)
                }
            }
        }

        // Export graph to DOT format
        val exporter = DOTExporter<Neuron, DefaultWeightedEdge>(
            { neuron -> neuron.hashCode().toString() } // Unique node IDs
        )

        // Add detailed attributes for neurons
        exporter.setVertexAttributeProvider { neuron ->
            val attributes = mutableMapOf<String, Attribute>()

            val neuronType = when {
                neuron in brain.sensoryNeurons -> "SensoryNeuron"
                neuron in brain.interneurons -> "Interneuron"
                neuron in brain.motorNeurons -> "MotorNeuron"
                else -> "UnknownNeuron"
            }

            // Assign colors based on neuron types
            val color = when (neuronType) {
                "SensoryNeuron" -> "green"
                "Interneuron" -> "blue"
                "MotorNeuron" -> "red"
                else -> "gray"
            }

            val extraDetails = when {
                neuron in sensesToNeurons -> "Sense: ${sensesToNeurons[neuron]}"
                neuron in motorNeuronsToActions -> "Action: ${motorNeuronsToActions[neuron]}"
                else -> ""
            }

            // Add attributes
            attributes["label"] = DefaultAttribute.createAttribute(
                "$neuronType\nID: ${neuron.hashCode()}\nActivation: %.2f\n%s".format(
                    neuron.activationValue, extraDetails
                )
            )
            attributes["type"] = DefaultAttribute.createAttribute(neuronType)
            attributes["color"] = DefaultAttribute.createAttribute(color) // Specify color
            attributes
        }

        // Add edge attributes (e.g., weights and colors)
        exporter.setEdgeAttributeProvider { edge ->
            val weight = graph.getEdgeWeight(edge)

            // Determine edge color and thickness based on weight
            val (color, penwidth) = when {
                weight < -1 -> "red" to "1.0"
                weight < 0 -> "orange" to "1.0"
                weight > 1 -> "green" to "1.0"
                weight > -1 && weight < 1 -> "blue" to "0.5"
                else -> "gray" to "0.5"
            }

            mapOf(
                "label" to DefaultAttribute.createAttribute("Weight: %.2f".format(weight)),
                "color" to DefaultAttribute.createAttribute(color),        // Set edge color
                "penwidth" to DefaultAttribute.createAttribute(penwidth)  // Set edge thickness
            )
        }

        val writer = StringWriter()
        exporter.exportGraph(graph, writer)
        return writer.toString()
    }
}
