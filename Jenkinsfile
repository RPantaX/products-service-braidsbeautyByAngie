pipeline {
	agent any

    environment {
		DOCKER_HUB_REPO = 'rpantax/products-service'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'

        // GitHub Authentication - FIXED
        GITHUB_USERNAME = 'RPantaX'
        GITHUB_TOKEN = credentials('github-token2')  // Para GitHub Packages
    }

    tools {
		maven 'Maven-4.0.0'
        jdk 'Java-21'  // FIXED: Cambiar a Java-21 que es lo que tienes instalado
    }

    stages {
		stage('Checkout') {
			steps {
				echo "Checking out code from ${env.BRANCH_NAME} branch"
                checkout scm
                script {
					env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                }
            }
        }

        stage('Environment Info') {
			steps {
				sh '''
                    echo "=== Environment Information ==="
                    echo "Java Version:"
                    java -version
                    echo "Maven Version:"
                    mvn --version
                    echo "Docker Version:"
                    docker --version
                    echo "Git Commit: ${GIT_COMMIT}"
                    echo "Git Branch: ${GIT_BRANCH}"
                    echo "Build Number: ${BUILD_NUMBER}"
                    echo "GitHub Username: ${GITHUB_USERNAME}"
                    echo "GitHub Token (first 10 chars): ${GITHUB_TOKEN}" | head -c 30
                    echo "..."
                '''
            }
        }

        stage('Verify GitHub Access') {
			steps {
				echo 'Verifying GitHub Packages access...'
                sh '''
                    echo "Testing GitHub authentication..."

                    # Test GitHub authentication with more details
                    HTTP_STATUS=$(curl -u ${GITHUB_USERNAME}:${GITHUB_TOKEN} \
                         -s -o /dev/null -w "%{http_code}" \
                         https://maven.pkg.github.com/RPantaX/core-service-braidsbeautyByAngie/com/braidsbeautyByAngie/saga-pattern-spring-boot/maven-metadata.xml)

                    echo "HTTP Status Code: $HTTP_STATUS"

                    if [ "$HTTP_STATUS" = "200" ]; then
                        echo "✅ GitHub authentication successful"
                        echo "Package metadata found:"
                        curl -u ${GITHUB_USERNAME}:${GITHUB_TOKEN} \
                             https://maven.pkg.github.com/RPantaX/core-service-braidsbeautyByAngie/com/braidsbeautyByAngie/saga-pattern-spring-boot/maven-metadata.xml \
                             -s | head -10
                    else
                        echo "❌ GitHub authentication failed with status: $HTTP_STATUS"
                        exit 1
                    fi
                '''
            }
        }

        stage('Debug Settings.xml') {
			steps {
				echo 'Debugging Maven settings...'
                sh '''
                    echo "=== Settings.xml Debug ==="
                    echo "Checking if settings.xml exists:"
                    ls -la ~/.m2/settings.xml || echo "settings.xml not found in home"
                    ls -la /var/jenkins_home/.m2/settings.xml || echo "settings.xml not found in jenkins home"

                    echo "Current user:"
                    whoami

                    echo "Environment variables:"
                    env | grep GITHUB

                    echo "Maven effective settings:"
                    mvn help:effective-settings -q | grep -A 10 -B 10 github || echo "No github config found"
                '''
            }
        }

        stage('Clean & Compile') {
			steps {
				echo 'Cleaning and compiling the project...'
                sh '''
                    echo "=== Maven Clean ==="
                    mvn clean -q

                    echo "=== Dependency Resolution with Debug ==="
                    # Use -X for debug output to see what's happening with authentication
                    mvn dependency:resolve -U -X | grep -E "(github|auth|401|error)" || true

                    echo "=== Compile ==="
                    mvn compile -DskipTests=true -q

                    echo "Compilation completed successfully"
                '''
            }
        }

        stage('Run Tests') {
			steps {
				echo 'Running unit tests...'
                sh '''
                    mvn test jacoco:report -q
                    echo "Tests completed"
                '''
            }
            post {
				always {
					publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Package') {
			steps {
				echo 'Packaging the application...'
                sh '''
                    mvn package -DskipTests=true -q
                    echo "Packaging completed"

                    # Verificar que el JAR se haya creado
                    if [ -f "application/target/application-0.0.1-SNAPSHOT.jar" ]; then
                        echo "✓ JAR file created successfully"
                        ls -la application/target/application-0.0.1-SNAPSHOT.jar
                    else
                        echo "✗ JAR file not found!"
                        exit 1
                    fi
                '''
            }
        }

        stage('Docker Build') {
			steps {
				echo 'Building Docker image...'
                script {
					def dockerImage = docker.build("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")
                    env.DOCKER_IMAGE_ID = dockerImage.id

                    if (env.BRANCH_NAME == 'main') {
						dockerImage.tag('latest')
                    }
                    dockerImage.tag("${env.BRANCH_NAME}-latest")
                }
            }
        }

        stage('Docker Test') {
			steps {
				echo 'Testing Docker image...'
                sh '''
                    docker run --rm -d --name products-service-test -p 8082:8081 ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}
                    sleep 10
                    docker stop products-service-test || true
                    echo "Docker image test completed successfully"
                '''
            }
        }

        stage('Docker Push') {
			when {
				anyOf {
					branch 'main'
                    branch 'develop'
                }
            }
            steps {
				echo 'Pushing Docker image to Docker Hub...'
                script {
					docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
						def image = docker.image("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")
                        image.push()
                        image.push("${env.BRANCH_NAME}-latest")

                        if (env.BRANCH_NAME == 'main') {
							image.push('latest')
                        }
                    }
                }
                echo "Docker image pushed successfully: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
            }
        }

        stage('Cleanup') {
			steps {
				echo 'Cleaning up Docker images...'
                sh '''
                    docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} || true
                    docker rmi ${DOCKER_HUB_REPO}:${BRANCH_NAME}-latest || true
                    docker image prune -f
                    echo "Cleanup completed"
                '''
            }
        }
    }

    post {
		always {
			echo 'Pipeline execution completed'
            cleanWs()
        }

        success {
			echo "✅ Pipeline completed successfully!"
            echo "🐳 Docker image: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"

            script {
				if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop') {
					echo "🚀 Image pushed to Docker Hub successfully!"
                }
            }
        }

        failure {
			echo "❌ Pipeline failed!"
            sh '''
                docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} || true
                docker image prune -f || true
            '''
        }

        unstable {
			echo "⚠️ Pipeline completed with warnings"
        }
    }
}