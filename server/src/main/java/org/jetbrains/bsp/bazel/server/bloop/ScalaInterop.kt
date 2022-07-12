package org.jetbrains.bsp.bazel.server.bloop

import scala.Option
import scala.Some
import scala.collection.immutable.List
import scala.collection.immutable.`List$`
import scala.jdk.javaapi.CollectionConverters.asScala
import java.nio.file.Path

object ScalaInterop {
    fun <T> emptyList(): List<T> = `List$`.`MODULE$`.empty()

    fun <T> emptyOption(): Option<T> = Option.empty()

    fun <T> T?.toOption(): Option<T> =
        this?.let { Some.apply(it) } ?: Option.empty()

    fun Path.toScalaList(): List<Path> =
        listOf(this).toScalaList()

    fun <T> Iterable<T>.toScalaList(): List<T> =
        asScala(this).toList()

    fun <T> Iterable<T>.toListOption(): Option<List<T>> =
        asScala(this).toList().toOption()

    fun <T> Iterator<T>.toScalaList(): List<T> =
        asScala(this).toList()

    fun <T> scalaListOf(vararg rest: T): List<T> =
        rest.toList().toScalaList()
}
