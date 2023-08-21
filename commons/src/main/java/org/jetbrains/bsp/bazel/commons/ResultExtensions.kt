package org.jetbrains.bsp.bazel.commons

fun <T> Result.Companion.of(block: () -> T): Result<T> =
  try {
    success(block())
  } catch (e: Exception) {
    failure(e)
  }

fun <T> List<Result<T>>.sequence(): Result<List<T>> {
  val successes = mutableListOf<T>()
  for (result in this) {
    if (result.isFailure) {
      return Result.failure(result.exceptionOrNull()!!)
    }
    successes.add(result.getOrNull()!!)
  }
  return Result.success(successes)
}

fun <T, R> Result<T>.flatMap(block: (T) -> Result<R>): Result<R> =
  if (isFailure) {
    Result.failure(exceptionOrNull()!!)
  } else {
    block(getOrNull()!!)
  }

fun <T> Result<T>.toList(): List<T> {
  return if (isFailure) {
    emptyList()
  } else {
    val value = getOrNull()
    if (value == null) {
      emptyList()
    } else {
      listOf(value)
    }
  }
}