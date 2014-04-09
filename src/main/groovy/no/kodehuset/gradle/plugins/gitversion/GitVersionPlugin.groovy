package no.kodehuset.gradle.plugins.gitversion

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that will set the project version (if not already set) by inspecting the currently checked out git
 * context.
 */
class GitVersionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (project.version != 'unspecified') {
            return
        }
        try {
            project.version = versionFromGit
        } catch (Exception e) {
            println "Unable to get version from git: $e.message"
        }
    }


    static String getVersionFromGit() {
        // 'git branch' typically has output like:
        //
        //   4.3
        // * 4.4
        //   feature/EH-3-messaging-system
        //   feature/client-connected
        //   master
        //
        // the sed expression first finds the line that starts with a *, then removes the * so that only the
        // branch name remains.
        def version = execute("git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \\(.*\\)/\\1/'")

        if (version == "(no branch)" || version.contains("detached from")) {
            // if the version contains 'no branch' or 'detached from', we are (always?) on a tag. If that is the case,
            // use the tag name as version name (with a few modifications - see below).
            version = execute("git describe --tags 2> /dev/null")
        } else {
            // we are on a branch. Use the branch name + "-SNAPSHOT" as version name
            version += "-SNAPSHOT"
        }
        version = version.replaceAll("/", "-")
        version = version.replace('version-', '')
        version
    }


    static String execute(String cmd) {
        Process p = new ProcessBuilder('/bin/sh', '-c', cmd).start()
        return p.text.trim()
    }
}
