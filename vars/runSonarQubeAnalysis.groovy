#!usr/bin/env groovy
def call(){ 
	echo "Running SonarQube "
	withSonarQubeEnv(credentialsId: 'SonarQube') {
		echo "Running SonarQube Analysis..."
		sh 'chmod +x gradlew'
		sh "./gradlew sonar"
	}
}
