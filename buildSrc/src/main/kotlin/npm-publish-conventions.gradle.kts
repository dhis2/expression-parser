import dev.petuska.npm.publish.extension.domain.NpmAccess

plugins {
    id("dev.petuska.npm.publish")
}

project.afterEvaluate {
    npmPublish {
        access.set(NpmAccess.PUBLIC)
        packages {
            named("js") {
                scope.set("dhis2")
                readme.set(File("./README.md"))
                packageJson {
                    "module" by "${project.name}.mjs"
                    "main" by "${project.name}.js"
                    "exports" by {
                        "." by {
                            "import" by "./${project.name}.mjs"
                            "require" by "./${project.name}.js"
                        }
                    }
                    "author" by "${Props.AUTHOR_NAME} <${Props.AUTHOR_EMAIL}>"
                    "description" by Props.DESCRIPTION
                    "license" by Props.LICENSE_NAME
                    "repository" by {
                        "type" by Props.REPOSITORY_TYPE
                        "url" by Props.REPOSITORY_URL
                    }
                    "publishConfig" by {
                        "access" by "public"
                    }
                    "private" by false
                }
            }
        }
    }
}
