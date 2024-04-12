load(":generator.bzl", "animals_library")

java_library(
    name = "consumer",
    srcs = glob(["src/java/org/jetbrains/bsp/example/consumer/animals/LibraryConsumer.java"]),
    deps = [":animals"],
)

# This is a jvm library that doesn't have sources. We must ensure we import it as a library, not as sources
# even if it is directly referred in projectview's targets section.
animals_library(
    name = "animals",
)
