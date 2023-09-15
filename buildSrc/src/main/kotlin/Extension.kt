import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.withType

fun Project.publicationConfig(dokkaHtml: TaskProvider<Jar>) {
    publishing.run {
        publications.withType<MavenPublication> {
            artifact(dokkaHtml)

            pom {
                name.set("DHIS Expression Parser")
                description.set("Expression Parser")

                organization {
                    name.set("UiO")
                    url.set("http://www.dhis2.org")
                }
                developers {
                    developer {
                        name.set("Jan Bernitt")
                        email.set("jbernitt@dhis2.org")
                        organization.set("UiO")
                        organizationUrl.set("http://www.uio.no/")
                    }
                }
                licenses {
                    license {
                        name.set("BSD")
                        url.set("http://opensource.org/licenses/BSD-2-Clause")
                    }
                }
                scm {
                    url.set("lp:dhis2")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/dhis2/dhis2-expression-parser")
                }
                url.set("https://github.com/dhis2/dhis2-expression-parser")
            }
        }
    }

}

val Project.publishing: PublishingExtension
    get() = extensions.findByName("publishing") as? PublishingExtension
        ?: error("Project '$name' is not a Publishable module")
