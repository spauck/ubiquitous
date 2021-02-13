# github_repo(
#   name = "pleasings",
#   repo = "thought-machine/pleasings",
#   revision = "master",
# )

subinclude("@pleasings/kotlin")

kotlin_library(
    name = "lib",
    srcs = [glob(["src/main/**/*.kt"])],
)

# kotlin_binary(
#     name = "bin",
#     deps = [":lib"],
# )

kotlin_test(
    name = "test",
    srcs = ["test.kt"],
    deps = [
        ":lib",
        "//third_party/java:junit",
        "//third_party/kotlin:kotlin_test",
    ],
)
