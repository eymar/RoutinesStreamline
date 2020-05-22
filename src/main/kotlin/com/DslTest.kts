#!/usr/bin/env kscript

@file:DependsOn("com.eymar:routines-streamline:0.0.7")

import com.routinesstreamliner.*

routines(args = args) {

    val componentName = paramFromStdin(hint = "Enter component name: ")

    val className = componentName.map {
        "${it}Component"
    }

    val newClass = newFileFromTemplate {
        templateParams {
            this["ClassName"] = className
        }

        executableIf { componentName.map { it.length > 1 } }

        fromTemplate(path = "../../../../templates/ExampleTemplate.kt")

        saveTo(path = className.map {
            "../generated/$it.kt"
        })

        dependsOn(className, componentName)
    }

    val newTests = newFileFromTemplate {
        val testClassName = className.map {
            "${it}Tests"
        }
        templateParams {
            this["TestClassName"] = testClassName
        }

        executableIf { componentName.map { it.length > 1 } }

        fromTemplate(path = "../../../../templates/ExampleTestsTemplate.kt")

        saveTo(path = testClassName.map {
            "../generated/$it.kt"
        })

        dependsOn(className, componentName, testClassName)
    }

    val appendFile = insertIntoFile {
        val templateName = paramFromStdin(hint = "Template Name value = ")

        executableIf { templateName.map { it.length > 1 } }

        appendFile("Test.txt")
        insertFrom {
            InsertFromSource.sourceFromTemplate(
                templateInput = "Hello, {{name}}".toByteArray().inputStream(),
                templateParams = { mapOf("name" to templateName) }
            )
        }

        dependsOn(templateName)
    }

    group(groupName = "New class and tests from templates") {
        +newClass
        +newTests
    }

    group(groupName = "Insert Hello-string into Test.txt") {
        +appendFile
    }

    group(groupName = "Common group (all routines)") {
        +newClass
        +newTests
        +appendFile
    }
}