# This says that all targets in this package that don't specify otherwise
# will be visible to the whole repo.
# This is often useful for third-party packages like this where we expect
# anyone to use them.
package(default_visibility = ["PUBLIC"])

# This fetches new build rules from an external source.
# In this case maven_jars, which we use below to fetch all transitive dependencies
# of a Maven package.
subinclude("//build_defs:maven_jars")

# This is temporarily necessary to work around a small bug.
package(maven_tool = "///pleasings//java/maven:please_maven")

# maven_jar fetches a single .jar file from Maven.
# Note that it doesn't fetch dependencies, we have to specify them explicitly.
# maven_jars fetches a library and its transitive dependencies from Maven.
maven_jars(
    name = "slf4j-simple",
    id = "org.slf4j:slf4j-simple:1.7.26",
)

#   implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
#   implementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")

maven_jars(
    name = "jackson-databind",
    id = "com.fasterxml.jackson.core:jackson-databind:2.9.8",
)

# maven_jar(
#     name = "mockito",
#     id = "org.mockito:mockito-all:1.10.19",
#     hash = "e93ceaea4cf704350fbb2a450ddbb7b4e3e062e3",
#     # This means that this library will only be able to be used by tests
#     test_only = True,
# )
