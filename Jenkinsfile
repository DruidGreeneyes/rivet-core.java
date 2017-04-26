pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'mvn clean test package'
                sh 'ls rivet-core.java/target/surefire-reports'
                junit 'rivet-core.java/target/surefire-reports/*.xml'
            }
        }
    }
}