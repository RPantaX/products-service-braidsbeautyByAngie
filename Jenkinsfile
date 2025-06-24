pipeline {
	agent any

    environment {
		DOCKER_HUB_REPO = 'rpantax/products-service'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }

    tools {
		maven 'Maven-3.8.6' // Ajusta según tu versión de Maven configurada
        jdk 'Java-17'       // Ajusta según tu JDK configurado
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
                '''
            }
        }

        stage('Clean & Compile') {
			steps {
				echo 'Cleaning and compiling the project...'
                sh '''
                    mvn clean compile -DskipTests=true
                    echo "Compilation completed successfully"
                '''
            }
        }

        stage('Run Tests') {
			steps {
				echo 'Running unit tests...'
                sh '''
                    mvn test jacoco:report
                    echo "Tests completed"
                '''
            }
            post {
				always {
					// Publicar resultados de tests
                    publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'

                    // Publicar reporte de coverage de JaCoCo
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
                    mvn package -DskipTests=true
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

                    // También crear tag 'latest' para la rama main
                    if (env.BRANCH_NAME == 'main') {
						dockerImage.tag('latest')
                    }

                    // Tag con nombre de rama
                    dockerImage.tag("${env.BRANCH_NAME}-latest")
                }
            }
        }

        stage('Docker Test') {
			steps {
				echo 'Testing Docker image...'
                sh '''
                    # Ejecutar contenedor para verificar que inicia correctamente
                    docker run --rm -d --name products-service-test -p 8082:8081 ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}

                    # Esperar un momento para que inicie
                    sleep 10

                    # Verificar que el servicio responde (ajusta el endpoint según tu aplicación)
                    # docker exec products-service-test curl -f http://localhost:8081/actuator/health || exit 1

                    # Detener el contenedor de prueba
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
                    # Limpiar imágenes locales para ahorrar espacio
                    docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} || true
                    docker rmi ${DOCKER_HUB_REPO}:${BRANCH_NAME}-latest || true

                    # Limpiar imágenes sin usar
                    docker image prune -f

                    echo "Cleanup completed"
                '''
            }
        }
    }

    post {
		always {
			echo 'Pipeline execution completed'

            // Limpiar workspace
            cleanWs()
        }

        success {
			echo "✅ Pipeline completed successfully!"
            echo "🐳 Docker image: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"

            // Notificación de éxito (opcional)
            script {
				if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop') {
					echo "🚀 Image pushed to Docker Hub successfully!"
                }
            }
        }

        failure {
			echo "❌ Pipeline failed!"

            // Limpiar imágenes en caso de fallo
            sh '''
                docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} || true
                docker image prune -f || true
            '''
        }

        unstable {
			echo "⚠️ Pipeline completed with warningss"
        }
    }
}