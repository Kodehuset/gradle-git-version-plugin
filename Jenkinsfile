node {
    stage('Checkout') {
        checkout scm
    }

    stage('Build and test') {

        if (env.BRANCH_NAME == 'master') {
            sh './gradlew clean build uploadArchives -DPASSWORD=jnhTyA'
        } else {
            sh './gradlew clean build'
        }
    }
}