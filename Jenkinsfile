pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'mvn clean test package'
                sh 'ls'
                junit 'rivet-core.java/target/surefire-reports/*.xml'
            }
        }
    }
}