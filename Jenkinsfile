pipeline {
	agent any

    environment {
		DOCKER_HUB_REPO = 'rpantax/products-service'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        // GitHub Authentication - FIXED
        GITHUB_USERNAME = 'RPantaX'
        GITHUB_TOKEN = credentials('github-token-2')  // Para GitHub Packages
        CURRENT_BRANCH = "${env.BRANCH_NAME ?: 'main'}"
    }

    tools {
		maven 'maven4.0.0'
    }
    stages {
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
        stage('Clone Repo') {
			steps {
				echo "Checking out code from ${env.CURRENT_BRANCH} branch"
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/developer']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/RPantaX/products-service-braidsbeautyByAngie.git',
                        credentialsId: 'github-token'
                    ]]
                ])
                script {
					// Obtener información del commit y rama actual
                    env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.CURRENT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    env.DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    echo "Building from branch: ${env.CURRENT_BRANCH}"
                    echo "Git commit: ${env.GIT_COMMIT}"
                    echo "Docker tag: ${env.DOCKER_IMAGE_TAG}"
                }
            }
        }
stage('Clean & Compile') {
			steps {
				echo 'Cleaning and compiling the project...'
                withCredentials([string(credentialsId: 'github-token-2', variable: 'GITHUB_TOKEN')]) {
					sh '''
                        echo "=== Generating Maven settings.xml ==="
                        cat > settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>RPantaX</username>
      <password>${GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
EOF

                        echo "=== VERIFICACIÓN PRE-COMPILACIÓN ==="
                        echo "Maven version:"
                        mvn -version

                        echo -e "\nJava version:"
                        java -version

                        echo -e "\n=== COMPILACIÓN ==="
                        mvn clean package --settings settings.xml

                    '''
                }
            }
        }

        stage('Docker Build') {
			steps {
				echo 'Building Docker image...'
                script {
					def dockerImage = docker.build("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")
                    env.DOCKER_IMAGE_ID = dockerImage.id

					dockerImage.tag("${env.BRANCH_NAME}-latest")
                    if (env.BRANCH_NAME == 'main') {
						dockerImage.tag('latest')
                    }
					echo "Docker image built successfully: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
                }
            }
        }

        stage('Docker Push') {
			when {
				anyOf {
					branch 'main'
                    branch 'developer'
                    // Agregar condición para cuando BRANCH_NAME sea null pero estemos en main
                    expression { env.CURRENT_BRANCH == 'main' }
                    expression { env.CURRENT_BRANCH == 'developer' }
                }
            }
            steps {
				echo 'Pushing Docker image to Docker Hub...'
                script {
					docker.withRegistry('https://index.docker.io/v1/', 'jenkins-cicd-token2') {
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
				echo 'Cleaning up local Docker images...'
                script {
					// Limpiar imágenes locales para ahorrar espacio
                    sh """
                        docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} || true
                        docker rmi ${DOCKER_HUB_REPO}:${env.CURRENT_BRANCH}-latest || true
                        if [ "${env.CURRENT_BRANCH}" = "main" ]; then
                            docker rmi ${DOCKER_HUB_REPO}:latest || true
                        fi
                    """
                }
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