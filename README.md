# Shared Library for Jenkins Pipelines

This repository contains a shared library for Jenkins pipelines. It provides reusable pipeline steps to streamline and standardize the CI/CD process. The library is organized into Groovy scripts stored in the `vars` folder.

## Structure

The shared library repository has the following structure:

```
.
├── README.md
└── vars
    ├── checkoutRepo.groovy
    ├── runUnitTests.groovy
    ├── codeCompile.groovy
    ├── runSonarQubeAnalysis.groovy
    ├── buildDockerImage.groovy
    ├── pushDockerImage.groovy
    ├── deployOnOpenShift.groovy
```

### `vars` Folder

The `vars` folder contains Groovy scripts, each representing a reusable step in the Jenkins pipeline. Below are the details of each script:

1. **checkoutRepo.groovy**

   ```groovy
   #!usr/bin/env groovy
   def call() {
       echo "Checking Git Repo..."
       git branch: 'dev', credentialsId: 'GitHub', url: 'https://github.com/Osamaomera/MultiCloudDevOpsProject.git'
   }
   ```

   - This script checks out the specified Git repository using the provided branch and credentials.

2. **runUnitTests.groovy**

   ```groovy
   #!usr/bin/env groovy
   def call() {
       echo "Running Unit Tests..."
       sh './gradlew test'
   }
   ```

   - This script runs unit tests using Gradle.

3. **codeCompile.groovy**

    ```groovy
    #!/usr/bin/env groovy
    def call() {
        echo "Running Code Compile..."
        sh 'mvn clean compile'	
    }
    ```

4. **runSonarQubeAnalysis.groovy**

   ```groovy
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
   ```

   - This script runs a SonarQube analysis on the specified project.

5. **buildDockerImage.groovy**

   ```groovy
   #!usr/bin/env groovy
    def call(String dockerHubCredentialsID, String imageName) {
        // Log in to DockerHub 
        withCredentials([usernamePassword(credentialsId: "${dockerHubCredentialsID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh "docker login -u ${USERNAME} -p ${PASSWORD}"        
            // Build and push Docker image
            echo "Building Docker image..."
            sh "docker build -t ${imageName}:${BUILD_NUMBER} ."	 
        }
    }

   ```

   - This script builds a Docker image using the provided image name.

6. **pushDockerImage.groovy**

   ```groovy
   #!usr/bin/env groovy
    def call(String dockerHubCredentialsID, String imageName) {
        // Log in to DockerHub 
        withCredentials([usernamePassword(credentialsId: "${dockerHubCredentialsID}", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            sh "docker login -u ${USERNAME} -p ${PASSWORD}"        
            // Build and push Docker image
            echo "Pushing Docker image..."
            sh "docker push ${imageName}:${BUILD_NUMBER}"	 
        }
    }

   ```

   - This script pushes a Docker image to DockerHub using the provided credentials.

7. **deployOnOpenShift.groovy**

   ```groovy
    #!/usr/bin/env groovy

    //OpenShiftCredentialsID can be credentials of service account token or KubeConfig file 
    def call(String OpenShiftCredentialsID, String openshiftClusterurl, String openshiftProject, String imageName) {
        
        // Update deployment.yaml with new Docker Hub image
        sh "sed -i 's|image:.*|image: ${imageName}:${BUILD_NUMBER}|g' deployment.yaml"

        withKubeCredentials(kubectlCredentials: [[caCertificate: '', clusterName: '', contextName: '', credentialsId: 'kubernetes', namespace: '', serverUrl: '']]) 
                sh"""
                    kubectl apply -f .
                """
                }

   ```

   - This script deploys an application to a Kubernetes cluster using the provided kubeconfig credentials and deployment file.


## Usage

To use this shared library in your Jenkins pipeline, you need to include the following in your `Jenkinsfile`:

```groovy
@Library('shared_library')_
pipeline {
    
    agent any
    
    tools {
        jdk 'jdk-17'
        maven 'maven'
    }

    environment {
        SCANNER_HOME                = tool 'sonar-scanner'
        dockerHubCredentialsID	    = 'DockerHub'  		    			         // DockerHub credentials ID.
	    imageName                   = 'osayman74/spot'                           // DockerHub repo/image_name.
        k8sCredentialsID	        = 'k8s'	    				                     // KubeConfig credentials ID.
    }

    triggers {
        githubPush() // Trigger pipeline on GitHub push events
    }
    
    stages {
                        
        stage('Repo Checkout') {
            steps {
            	script {
                	checkoutRepo
                }
            }
        }

        stage('Run Unit Test') {
            steps {
                script {
                	// Navigate to the directory contains the Application
                	dir('App') {
                		runUnitTests
            		}
        	   }
    	    }
	    }

        stage('Run Code Compile') {
            steps {
                script {
                	// Navigate to the directory contains the Application
                	dir('App') {
                		codeCompile
            		}
        	    }
    	    }
	    }
	
        stage('Run SonarQube Analysis') {
            steps {
                script {
                    	// Navigate to the directory contains the Application
                    	dir('app') {
                    		runSonarQubeAnalysis()
                    	}
                    }
                }
            }

        stage('Build Docker Image') {
            steps {
                script {
                	// Navigate to the directory contains Dockerfile
                 	dir('app') {
                 		buildDockerImage("${dockerHubCredentialsID}", "${imageName}")
                        
                    	}
                    }
                }
            }

        stage('Push Docker Image') {
            steps {
                script {
                	// Navigate to the directory contains Dockerfile
                 	dir('app') {
                 		pushDockerImage("${dockerHubCredentialsID}", "${imageName}")
                        
                    	}
                }
            }
        }

        stage('Deploy on OpenShift Cluster') {
            steps {
                script { 
                        // Navigate to the directory contains OpenShift YAML files
                	dir('k8s') {
				deployOnOpenShift("${openshiftCredentialsID}", "${openshiftCluster}", "${openshifProject}", "${imageName}")
                    	}
                }
            }
        }
    }

post {
        success {
            echo "${JOB_NAME}-${BUILD_NUMBER} pipeline succeeded"
        }
        failure {
            echo "${JOB_NAME}-${BUILD_NUMBER} pipeline failed"
        }
    }
}
```

## Contributing

If you would like to contribute to this shared library, please fork the repository, create a new branch, and submit a pull request.
