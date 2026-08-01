buildscript {
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
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
