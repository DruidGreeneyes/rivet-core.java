pipeline {
    agent any
    stages {
        stage('build') {
            steps {
                sh 'mvn clean test package'
                junit 'rivet-core.java/target/surefire-reports/*.xml'
            }
        }
    }
}