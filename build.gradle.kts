plugins {
    kotlin("multiplatform") version "1.9.0"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("io.freefair.lombok") version "8.3"
    id("org.jetbrains.dokka") version "1.9.0"
}

repositories {
    mavenCentral()
}

group = "org.hisp.dhis.lib.expression"
version = "1.1.0-SNAPSHOT"

val isReleaseVersion = project.hasProperty("removeSnapshot")
if (isReleaseVersion) {
    version = (version as String).replace("-SNAPSHOT", "")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        nodejs()
        useEsModules()
        binaries.library()
    }
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

// Publication

val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
val signingPrivateKey: String? = System.getenv("SIGNING_PRIVATE_KEY")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")

val dokkaHtml = tasks.findByName("dokkaHtml")!!

val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputs)
    archiveClassifier.set("docs")
}

publicationConfig(dokkaHtmlJar)

nexusPublishing {
    this.repositories {
        sonatype {
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

signing {
    isRequired = isReleaseVersion
    useInMemoryPgpKeys(signingPrivateKey, signingPassword)
    sign(publishing.publications)
}