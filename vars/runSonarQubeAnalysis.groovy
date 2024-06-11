#!usr/bin/env groovy
def call(){ 
	echo "Running SonarQube "
	withSonarQubeEnv(credentialsId: 'SonarQube') {
		echo "Running SonarQube Analysis..."
		sh ''' $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=Devops-CICD \
		-Dsonar.java.binaries=. \
		-Dsonar.projectKey=Devops-CICD '''
	}
}
