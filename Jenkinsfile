pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'mvn clean test package'
                sh 'ls'
                junit 'target/surefire-reports/*.xml'
            }
        }
    }
}