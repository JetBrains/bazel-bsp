package org.jetbrains.bsp.bazel.projectview.model

import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import io.vavr.collection.List
import io.vavr.collection.Traversable

data class TargetSpecs(
    val included: List<BuildTargetIdentifier>,
    val excluded: List<BuildTargetIdentifier>
) {

    companion object {

        @JvmStatic
        fun empty(): TargetSpecs {
            return TargetSpecs(List.empty(), List.empty())
        }

        @JvmStatic
        fun of(included: Traversable<BuildTargetIdentifier>): TargetSpecs {
            return TargetSpecs(included.toList(), List.empty())
        }
    }
}
