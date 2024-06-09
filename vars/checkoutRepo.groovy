#!usr/bin/env groovy
def call() {
	echo "checking Git Repo..."
	checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'GitHub', url: 'https://github.com/Osamaomera/MultiCloudDevOpsProject.git']])	
}
