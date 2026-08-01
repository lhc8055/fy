pluginManagement {
    repositories {
        maven {
            url = uri("http://localhost:8083/google/")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://localhost:8083/central/")
            isAllowInsecureProtocol = true
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("http://localhost:8083/google/")
            isAllowInsecureProtocol = true
        }
        maven {
            url = uri("http://localhost:8083/central/")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "LiquidDock"
include(":backdrop")
include(":app")
