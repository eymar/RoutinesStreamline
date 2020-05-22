#!/usr/bin/env kscript

@file:DependsOn("com.eymar:routines-streamline:0.0.6")

import com.routinesstreamliner.*

routines(args = args) {
    val componentName = inputParam {
        Routines.stdin(hint = "Enter component name: ")
    }

    val className = ParamValue {
        "${componentName}Component"
    }

    newFileFromTemplate {
        val savePath = ParamValue {
            "../generated/$className.kt"
        }

        templateParams {
            this["ClassName"] = className.get()
        }

        executableIf { true }

        fromTemplate("../../../../templates/ExampleTemplate.kt")
        saveTo(savePath)
    }

    newFileFromTemplate {
        val testClassName = ParamValue {
            "${className}Tests"
        }

        val savePath = ParamValue {
            "../generated/tests/$testClassName.kt"
        }

        templateParams {
            this["TestClassName"] = testClassName.get()
        }

        executableIf { true }

        fromTemplate("../../../../templates/ExampleTestsTemplate.kt")
        saveTo(savePath)
    }

    insertIntoFile {
        val templateName = inputParam {
            Routines.stdin("Template Name value = ")
        }

        appendFile("Test.txt")
        insertFrom(
            InsertFromSource.sourceFromTemplate(
                templateInput = "Hello, {{name}}".toByteArray().inputStream(),
                templateParams = { mapOf("name" to templateName.get()) }
            )
        )
    }
}