#!/usr/bin/env kscript

@file:DependsOn("com.eymar:routines-streamline:0.0.7")

import com.routinesstreamliner.*

routines(args = args) {
    val componentName = paramFromStdin(hint = "Enter component name: ")

    val className = componentName.map {
        "${it}Component"
    }

    newFileFromTemplate {
        templateParams {
            this["ClassName"] = className
        }

        executableIf { componentName.map { it.length > 1 } }

        fromTemplate(path = "../../../../templates/ExampleTemplate.kt")

        saveTo(path = className.map {
            "../generated/$it.kt"
        })

        friendlyName { "Create class from ExampleTemplate.kt" }
    }

    newFileFromTemplate {
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

        friendlyName { "Create Tests from ExampleTestsTemplate.kt" }
    }

    insertIntoFile {
        val templateName = paramFromStdin(hint = "Template Name value = ")

        executableIf { templateName.map { it.length > 1 } }

        appendFile("Test.txt")
        insertFrom(source = InsertFromSource.sourceFromTemplate(
            templateInput = "Hello, {{name}}".toByteArray().inputStream(),
            templateParams = { mapOf("name" to templateName) }
        ))

        friendlyName { "Insert Hello-string into Test.txt" }
    }
}