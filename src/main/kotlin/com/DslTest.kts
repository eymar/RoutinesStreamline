#!/usr/bin/env kscript

@file:MavenRepository("repo","https://dl.bintray.com/eymar/generic" )
@file:DependsOn("com.eymar:routines-streamline:0.0.5")

import com.routinesstreamliner.*

routines {
    val componentName = ParamValue.stdin("ComponentName = ")
    val className = ParamValue.combine { componentName + "Component" }
    val testClassName = ParamValue.combine { className + "Tests" }

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

//    insertIntoFile {
//        appendFile("Te.txt")
//        insertFrom(
//            InsertFromSource.sourceFromTemplate(
//                templateInput = "Hello, {{name}}".toByteArray().inputStream(),
//                templateParams = mapOf("name" to "Sasha")
//            )
//        )
//    }
}