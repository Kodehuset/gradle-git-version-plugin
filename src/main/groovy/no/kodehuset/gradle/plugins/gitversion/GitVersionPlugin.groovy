package no.kodehuset.gradle.plugins.gitversion

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that will set the project version by inspecting the currently checked out git
 * context. If the project version property is already set, the plugin will only override it from git if you are
 * building from a tag and the version contains -SNAPSHOT.
 */
class GitVersionPlugin implements Plugin<Project> {

    static class GitVersion {
        String version
        boolean fromTag
    }


    @Override
    void apply(Project project) {

        try {
            GitVersion gitVersion = getVersionFromGit()

            println ""
            println project.version
            String specifiedVersion = project.version

            if (specifiedVersion == 'unspecified') {
                project.version = gitVersion.version
            } else if (specifiedVersion.contains('-SNAPSHOT')) {
                if (gitVersion.fromTag) {
                    project.version = gitVersion.version
                }
            } else {
                project.version = specifiedVersion
            }
            println project.version
        } catch (Exception e) {
            println "Unable to get version from git: $e.message"
        }
    }


    static GitVersion getVersionFromGit() {
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

        boolean fromTag = false
        if (version == "(no branch)" || version.contains("detached from")) {
            // if the version contains 'no branch' or 'detached from', we are (always?) on a tag. If that is the case,
            // use the tag name as version name (with a few modifications - see below).
            version = execute("git describe --tags 2> /dev/null")
            fromTag = true
        } else {
            // we are on a branch. Use the branch name + "-SNAPSHOT" as version name
            version += "-SNAPSHOT"
        }
        version = version.replaceAll("/", "-")
        version = version.replace('version-', '')
        new GitVersion(version: version, fromTag: fromTag)
    }


    static String execute(String cmd) {
        Process p = new ProcessBuilder('/bin/sh', '-c', cmd).start()
        return p.text.trim()
    }
}
