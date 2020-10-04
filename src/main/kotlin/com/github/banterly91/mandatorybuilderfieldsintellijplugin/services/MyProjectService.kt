package com.github.banterly91.mandatorybuilderfieldsintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.banterly91.mandatorybuilderfieldsintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
