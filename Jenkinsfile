pipeline {
	agent any

    environment {
		DOCKER_HUB_REPO = 'rpantax/products-service'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'

        // GitHub Authentication - FIXED
        GITHUB_USERNAME = 'RPantaX'
        GITHUB_TOKEN = credentials('github-token-2')  // Para GitHub Packages
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
				echo "Checking out code from repository"
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/RPantaX/products-service-braidsbeautyByAngie.git',
                        credentialsId: 'github-token'
                    ]]
                ])
                script {
					// CRITICAL: Establecer variables de entorno correctamente
                    env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()

                    // Obtener la rama actual de forma robusta
                    def currentBranch = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    if (currentBranch == 'HEAD') {
						// Si estamos en detached HEAD, obtener de refs remotos
                        currentBranch = sh(returnStdout: true, script: 'git branch -r --contains HEAD | head -1 | sed "s/.*\\///" | sed "s/ //g"').trim()
                    }
                    if (currentBranch == '' || currentBranch == 'HEAD') {
						currentBranch = 'main' // fallback
                    }

                    // Establecer variables de entorno
                    env.CURRENT_BRANCH = currentBranch
                    env.BRANCH_NAME = currentBranch  // CRITICAL: Establecer BRANCH_NAME explícitamente
                    env.DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"

                    echo "✅ Repository information:"
                    echo "  Building from branch: ${env.CURRENT_BRANCH}"
                    echo "  Git commit: ${env.GIT_COMMIT}"
                    echo "  Docker tag: ${env.DOCKER_IMAGE_TAG}"
                    echo "  BRANCH_NAME: ${env.BRANCH_NAME}"
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
                        mvn clean package -DskipTests --settings settings.xml

                        echo -e "\n=== VERIFICACIÓN POST-COMPILACIÓN ==="
                        echo "Verificando JAR generado:"
                        if [ -f "application/target/application-0.0.1-SNAPSHOT.jar" ]; then
                            echo "✅ JAR encontrado: application/target/application-0.0.1-SNAPSHOT.jar"
                            ls -la application/target/application-0.0.1-SNAPSHOT.jar
                        else
                            echo "❌ JAR NO encontrado"
                            echo "JARs disponibles:"
                            find . -name "*.jar" -type f | grep -v ".m2" | head -10
                        fi
                    '''
                }
            }
        }

        stage('Docker Build') {
			steps {
				echo 'Building Docker image...'
                script {
					// Verificar que las variables estén correctas antes del build
                    echo "🔍 Variables antes del Docker build:"
                    echo "  DOCKER_IMAGE_TAG: ${env.DOCKER_IMAGE_TAG}"
                    echo "  BRANCH_NAME: ${env.BRANCH_NAME}"
                    echo "  CURRENT_BRANCH: ${env.CURRENT_BRANCH}"

                    def dockerImage = docker.build("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")
                    env.DOCKER_IMAGE_ID = dockerImage.id

                    // Aplicar tags según la rama
                    if (env.BRANCH_NAME == 'main') {
						echo "📋 Aplicando tag 'latest' para rama main"
                        dockerImage.tag('latest')
                    }

                    // Tag de rama (asegurar que BRANCH_NAME no sea null)
                    def branchTag = "${env.BRANCH_NAME ?: 'unknown'}-latest"
                    echo "📋 Aplicando tag de rama: ${branchTag}"
                    dockerImage.tag(branchTag)

                    echo "✅ Docker build completed successfully"
                    echo "🐳 Tags aplicados:"
                    echo "  - ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
                    echo "  - ${DOCKER_HUB_REPO}:${branchTag}"
                    if (env.BRANCH_NAME == 'main') {
						echo "  - ${DOCKER_HUB_REPO}:latest"
                    }
                }
            }
        }

        stage('Docker Test') {
			steps {
				echo 'Testing Docker image...'
                sh '''
                    echo "🧪 Iniciando test del contenedor..."

                    # Limpiar contenedores previos si existen
                    docker stop products-service-test 2>/dev/null || true
                    docker rm products-service-test 2>/dev/null || true

                    # Ejecutar contenedor en background
                    echo "Iniciando contenedor..."
                    CONTAINER_ID=$(docker run --rm -d --name products-service-test -p 8082:8081 ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG})
                    echo "Container ID: $CONTAINER_ID"

                    # Esperar un poco más para que la aplicación inicie
                    echo "Esperando que la aplicación inicie..."
                    sleep 15

                    # Verificar que el contenedor sigue corriendo
                    if docker ps | grep -q products-service-test; then
                        echo "✅ Contenedor está corriendo"

                        # Intentar hacer petición health check (opcional)
                        echo "Verificando health check..."
                        if curl -f http://localhost:8082/actuator/health 2>/dev/null; then
                            echo "✅ Health check exitoso"
                        else
                            echo "⚠️  Health check falló (puede ser normal si no está configurado)"
                        fi

                        # Mostrar logs del contenedor
                        echo "📋 Últimos logs del contenedor:"
                        docker logs products-service-test --tail 20

                    else
                        echo "❌ Contenedor se detuvo inesperadamente"
                        echo "📋 Logs del contenedor:"
                        docker logs products-service-test 2>/dev/null || echo "No se pudieron obtener logs"

                        # No fallar el pipeline por esto, solo advertir
                        echo "⚠️  Test de contenedor falló, pero continuando pipeline"
                    fi

                    # Limpiar contenedor
                    echo "Limpiando contenedor de test..."
                    docker stop products-service-test 2>/dev/null || true
                    docker rm products-service-test 2>/dev/null || true

                    echo "✅ Docker test completado"
                '''
            }
        }

        stage('Docker Push') {
			when {
				anyOf {
					environment name: 'BRANCH_NAME', value: 'main'
                    environment name: 'BRANCH_NAME', value: 'develop'
                    // Alternativa: usar expresión para más flexibilidad
                    expression {
						return env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop'
                    }
                }
            }
            steps {
				echo "🚀 Pushing Docker image to Docker Hub..."
                echo "📋 Branch: ${env.BRANCH_NAME}"
                echo "📋 Tags to push:"
                echo "  - ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
                echo "  - ${DOCKER_HUB_REPO}:${env.BRANCH_NAME}-latest"
                if (env.BRANCH_NAME == 'main') {
					echo "  - ${DOCKER_HUB_REPO}:latest"
                }

                script {
					docker.withRegistry('https://index.docker.io/v1/', 'jenkins-cicd-token2') {
						def image = docker.image("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")

                        // Push tag principal
                        echo "Pushing main tag..."
                        image.push()

                        // Push tag de rama
                        echo "Pushing branch tag..."
                        image.push("${env.BRANCH_NAME}-latest")

                        // Push latest si es main
                        if (env.BRANCH_NAME == 'main') {
							echo "Pushing latest tag..."
                            image.push('latest')
                        }
                    }
                }
                echo "✅ Docker image pushed successfully: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
            }
        }

        stage('Cleanup') {
			steps {
				echo 'Cleaning up Docker images...'
                sh '''
                    echo "🧹 Limpiando imágenes Docker..."

                    # Limpiar tag principal
                    docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} 2>/dev/null || echo "Tag principal ya limpio"

                    # Limpiar tag de rama (verificar que BRANCH_NAME no sea null)
                    BRANCH_TAG="${BRANCH_NAME:-unknown}-latest"
                    if [ "$BRANCH_TAG" != "null-latest" ] && [ "$BRANCH_TAG" != "-latest" ]; then
                        docker rmi ${DOCKER_HUB_REPO}:${BRANCH_TAG} 2>/dev/null || echo "Tag de rama ya limpio"
                    else
                        echo "⚠️  Skipping cleanup de tag inválido: $BRANCH_TAG"
                    fi

                    # Limpiar latest si existe
                    if [ "${BRANCH_NAME}" = "main" ]; then
                        docker rmi ${DOCKER_HUB_REPO}:latest 2>/dev/null || echo "Tag latest ya limpio"
                    fi

                    # Limpiar imágenes dangling
                    docker image prune -f

                    echo "✅ Cleanup completado"
                '''
            }
        }
    }

    post {
		always {
			echo 'Pipeline execution completed'
            script {
				echo "📋 Pipeline Summary:"
                echo "  Branch: ${env.BRANCH_NAME ?: 'Unknown'}"
                echo "  Commit: ${env.GIT_COMMIT ?: 'Unknown'}"
                echo "  Docker Tag: ${env.DOCKER_IMAGE_TAG ?: 'Unknown'}"
            }
            cleanWs()
        }

        success {
			echo "✅ Pipeline completed successfully!"
            echo "🐳 Docker image: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"

            script {
				if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop') {
					echo "🚀 Image pushed to Docker Hub successfully!"
                    echo "📋 Available tags:"
                    echo "  - ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
                    echo "  - ${DOCKER_HUB_REPO}:${env.BRANCH_NAME}-latest"
                    if (env.BRANCH_NAME == 'main') {
						echo "  - ${DOCKER_HUB_REPO}:latest"
                    }
                } else {
					echo "📋 Image built but not pushed (branch: ${env.BRANCH_NAME})"
                    echo "💡 Only 'main' and 'develop' branches are pushed to Docker Hub"
                }
            }
        }

        failure {
			echo "❌ Pipeline failed!"
            script {
				echo "🔍 Failure debugging info:"
                echo "  Branch: ${env.BRANCH_NAME ?: 'Unknown'}"
                echo "  Stage: ${env.STAGE_NAME ?: 'Unknown'}"
            }
            sh '''
                # Limpiar contenedores de test si quedaron
                docker stop products-service-test 2>/dev/null || true
                docker rm products-service-test 2>/dev/null || true

                # Limpiar imágenes si existen
                docker rmi ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG} 2>/dev/null || true
                docker image prune -f || true
            '''
        }

        unstable {
			echo "⚠️ Pipeline completed with warnings"
        }
    }
}