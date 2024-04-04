package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildServer
import ch.epfl.scala.bsp4j.CppBuildServer
import ch.epfl.scala.bsp4j.JavaBuildServer
import ch.epfl.scala.bsp4j.JvmBuildServer
import ch.epfl.scala.bsp4j.PythonBuildServer
import ch.epfl.scala.bsp4j.RustBuildServer
import ch.epfl.scala.bsp4j.ScalaBuildServer

interface JoinedBuildServer : BuildServer, JvmBuildServer, ScalaBuildServer,
  JavaBuildServer, CppBuildServer, BazelBuildServer, PythonBuildServer, RustBuildServer