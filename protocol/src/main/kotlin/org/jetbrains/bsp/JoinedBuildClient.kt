package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildClient

interface JoinedBuildClient : BuildClient, BazelBuildClient
