import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.palantir.gradle.gitversion.VersionDetails
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    id("com.android.library") version AGP.version
    kotlin("multiplatform") version Kotlin.version

    id("com.palantir.git-version") version "0.12.3"

    id(Release.MavenPublish.plugin)
}

fun Project.kotlinSetup() {
    kotlin {
        jvm()
        androidTarget()
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser()
        }

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        applyDefaultHierarchyTemplate {
            common {
                group("jvm") {
                    withAndroidTarget()
                    withJvm()
                }
                withWasmJs()
            }
        }
    }

    android {
        namespace = Kotori.groupId
        compileSdk = AGP.compileSdkVersion
        defaultConfig {
            minSdk = AGP.minSdkVersion
        }
    }
}

kotlinSetup()

allprojects {
    repositories {
        mavenCentral()

        // https://github.com/sergeych/mp_stools
        maven("https://maven.universablockchain.com/")
    }

    // compile bytecode to java 8 (default is java 6)
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val sourcesJar by tasks

val versionDetails: groovy.lang.Closure<*> by extra
val gitVersionDetails: VersionDetails = versionDetails() as VersionDetails
val publishProjectPaths = listOf(":kotori", ":kotori-sudachi")
val multiplatformProjectPaths = listOf(":kotori")
subprojects {
    version = gitVersionDetails.lastTag + "-TEST"
    group = Kotori.groupId

    if (project.path in multiplatformProjectPaths) {
        apply {
            plugin("com.android.library")
            plugin("org.jetbrains.kotlin.multiplatform")
        }

        kotlinSetup()
    }

    if (project.path in publishProjectPaths) {

        apply {
            plugin(Release.MavenPublish.plugin)
        }

        publishing {
            publications {
                register(project.name, MavenPublication::class) {
//                    from(components["java"])
                    artifact(sourcesJar)

                    groupId = project.group as String
                    version = project.version as String?
                    artifactId = project.name

                    pom {
                        name.set(project.name)
                        description.set(Kotori.Package.desc)
                        packaging = "jar"
                        url.set(Kotori.Package.url)

                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("http://www.opensource.org/licenses/mit-license.php")
                            }
                        }
                        scm {
                            url.set(Kotori.Package.url)
                            connection.set(Kotori.Package.scm)
                            developerConnection.set(Kotori.Package.scm)
                        }
                    }
                }
            }
        }
    }
}

task("prepareTestingData") {
    dependsOn(
            ":kotori-dictionaries:downloadSudachiSmallDict",
            ":kotori-dictionaries:downloadMecabIpadic",
            ":kotori-benchmark:downloadLivedoorNews",
            ":kotori-benchmark:downloadTatoeba"
    )
}
