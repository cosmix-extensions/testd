import com.android.build.api.dsl.LibraryExtension
import app.cosmix.gradle.CosmixExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("com.github.cosmix-app:cosmix-gradle-plugin:1.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cosmix(configuration: CosmixExtension.() -> Unit) = extensions.getByName<CosmixExtension>("cosmix").configuration()

fun Project.android(configuration: LibraryExtension.() -> Unit) {
    extensions.getByName<LibraryExtension>("android").apply {
        project.extensions.findByType(JavaPluginExtension::class.java)?.apply {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }
        configuration()
    }
}

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "app.cosmix.gradle")

    cosmix {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "https://github.com/cosmix-extensions/test5")
    }

    android {
        namespace = "com.free"
        compileSdk = 34

        defaultConfig {
            minSdk = 21
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType<KotlinJvmCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_1_8)
                freeCompilerArgs.addAll(
                    "-Xno-call-assertions",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions"
                )
            }
        }
    }

    dependencies {
        val implementation by configurations
        val cosmix by configurations
        cosmix("com.github.cosmix-app:cosmix:pre-release")
        implementation(kotlin("stdlib"))
        implementation("org.jsoup:jsoup:1.16.1")
        implementation("com.google.code.gson:gson:2.10.1")
    }
}
