import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.andreypfau"
version = "1.0.0"

kotlin {
    explicitApi()

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    linuxArm64()
    linuxX64()

    macosArm64()
    macosX64()
    mingwX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }

    configureSourceSetsLayout()
    disablePlatformTests()
}

android {
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    if (!project.hasProperty("skipSigning")) {
        signAllPublications()
    }

    coordinates(group.toString(), "raptorq-kotlin", version.toString())

    pom {
        name = "RaptorQ Kotlin"
        description =
            "RaptorQ-Kotlin is a Kotlin Multiplatform library implementing the RaptorQ FEC algorithm (RFC 6330). It enables efficient symbol encoding/decoding for loss-resilient data transport in peer-to-peer or broadcast systems. Suitable for both client and server applications requiring robust data delivery."
        inceptionYear = "2024"
        url = "https://github.com/andreypfau/raptorq-kotlin/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "andreypfau"
                name = "Andrei Pfau"
                url = "https://github.com/andreypfau/"
                email = "andreypfau@gmail.com"
            }
        }
        scm {
            url = "https://github.com/andreypfau/raptorq-kotlin/"
            connection = "scm:git:git://github.com/andreypfau/raptorq-kotlin.git"
            developerConnection = "scm:git:ssh://github.com:andreypfau/raptorq-kotlin.git"
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSetsLayout() {
    sourceSets {
        all {
            if (name.endsWith("Main")) {
                val suffix = if (name.startsWith("common")) "" else "@${name.removeSuffix("Main")}"
                kotlin.srcDir("src$suffix")
                resources.srcDir("resources$suffix")
            }
            if (name.endsWith("Test")) {
                val suffix = if (name.startsWith("common")) "" else "@${name.removeSuffix("Test")}"
                kotlin.srcDir("test$suffix")
                resources.srcDir("testResources$suffix")
            }
        }
    }
}

fun KotlinMultiplatformExtension.disablePlatformTests() {
    listOf(
        "iosSimulatorArm64",
        "iosX64",
        "tvosSimulatorArm64",
        "tvosX64",
        "watchosSimulatorArm64",
        "watchosX64"
    ).forEach { targetName ->
        targets.findByName(targetName)?.let { target ->
            target.compilations.findByName("test")?.compileTaskProvider?.get()?.enabled = false
        }
    }

    afterEvaluate {
        tasks.configureEach {
            if (name.endsWith("Test") && listOf(
                    "iosSimulatorArm64",
                    "iosX64",
                    "tvosSimulatorArm64",
                    "tvosX64",
                    "watchosSimulatorArm64",
                    "watchosX64"
                ).any { arch -> name.startsWith(arch) }
            ) {
                enabled = false
            }
        }
    }
}
