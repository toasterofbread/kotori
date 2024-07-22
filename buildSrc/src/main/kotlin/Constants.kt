// Library version
object Kotori {
    const val groupId = "com.github.wanasit.kotori"

    object Package {
        const val repo = "maven"
        const val name = "Kotori"
        const val desc = "A Japanese tokenizer and morphological analysis engine written in Kotlin"
        const val userOrg = "wanasit"
        const val url = "https://github.com/wanasit/kotori"
        const val scm = "git@github.com:wanasit/kotori.git"
        const val licenseName = "MIT License"
    }
}

object Kotlin {
    const val version = "2.0.0"
    object Dependencies {
        const val Stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val Reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"

        const val Test = "org.jetbrains.kotlin:kotlin-test:$version"
        const val TestJunit = "org.jetbrains.kotlin:kotlin-test-junit:$version"
    }
}

object AGP {
    const val version = "8.2.0"
    const val minSdkVersion = 1
    const val compileSdkVersion = 34
}

object Kuromoji {
    const val version = "0.9.0"
    object Dependencies {
        const val Kuromoji_IPADIC = "com.atilika.kuromoji:kuromoji-ipadic:$version"
    }
}

object Okio {
    const val version = "3.9.0"
    object Dependencies {
        const val Okio = "com.squareup.okio:okio:$version"
    }
}

object Ktor {
    const val version = "3.0.0-beta-2"
    object Dependencies {
        const val Ktor = "io.ktor:ktor-client-core:$version"
    }
}

object KotlinCodepoints {
    const val version = "0.9.0"
    object Dependencies {
        const val KotlinCodepoints = "de.cketti.unicode:kotlin-codepoints:$version"
    }
}

object mp_stools {
    const val version = "1.4.7"
    object Dependencies {
        const val mp_stools = "net.sergeych:mp_stools:$version"
    }
}

object Sudachi {
    const val version = "0.4.0"
    object Dependencies {
        const val Sudachi = "com.worksap.nlp:sudachi:$version"
    }
}

object Release {
    object MavenPublish {
        const val plugin = "maven-publish"
    }

    object Bintray {
        const val version = "1.8.4"
        const val plugin = "com.jfrog.bintray"
    }
}