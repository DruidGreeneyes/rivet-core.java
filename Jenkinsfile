pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'mvn clean test package'
                junit 'target/surefire-reports/*.xml'
            }
        }
    }
}