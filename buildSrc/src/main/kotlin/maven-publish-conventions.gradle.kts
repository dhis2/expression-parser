plugins {
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.dokka")
}

val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
val signingPrivateKey: String? = System.getenv("SIGNING_PRIVATE_KEY")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")

val isReleaseVersion = (version as String).contains("SNAPSHOT")

val dokkaHtml = tasks.findByName("dokkaHtml")!!

val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(dokkaHtml)
    from(dokkaHtml.outputs)
    archiveClassifier.set("docs")
}

publishing {
    publications.withType<MavenPublication> {
        artifact(dokkaHtmlJar)

        pom {
            name.set("DHIS Expression Parser")
            description.set(Props.DESCRIPTION)

            organization {
                name.set(Props.ORGANIZATION_NAME)
                url.set(Props.ORGANIZATION_URL)
            }
            developers {
                developer {
                    name.set(Props.AUTHOR_NAME)
                    email.set(Props.AUTHOR_EMAIL)
                    organization.set(Props.ORGANIZATION_NAME)
                    organizationUrl.set(Props.ORGANIZATION_URL)
                }
            }
            licenses {
                license {
                    name.set(Props.LICENSE_NAME)
                    url.set(Props.LICENSE_URL)
                }
            }
            scm {
                url.set("lp:dhis2")
            }
            issueManagement {
                system.set(Props.REPOSITORY_SYSTEM)
                url.set(Props.REPOSITORY_URL)
            }
            url.set(Props.REPOSITORY_URL)
        }
    }
}

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
