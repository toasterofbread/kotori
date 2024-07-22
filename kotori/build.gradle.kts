plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(Okio.Dependencies.Okio)
                implementation(Ktor.Dependencies.Ktor)
                implementation(KotlinCodepoints.Dependencies.KotlinCodepoints)
                implementation(mp_stools.Dependencies.mp_stools)
            }
        }
    }
}

dependencies {
    implementation(Kotlin.Dependencies.Stdlib)

    testImplementation(Kotlin.Dependencies.Test)
    testImplementation(Kotlin.Dependencies.TestJunit)
    testImplementation(Kuromoji.Dependencies.Kuromoji_IPADIC)
}
