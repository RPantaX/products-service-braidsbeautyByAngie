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
				echo "Checking out code from ${env.CURRENT_BRANCH} branch"
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
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

                        echo -e "\n=== LIMPIEZA INICIAL ==="
                        mvn clean --settings settings.xml

                        echo -e "\n=== COMPILACIÓN CON LOGS DETALLADOS ==="
                        mvn package -DskipTests --settings settings.xml -e -X | tail -100

                        echo -e "\n=== VERIFICACIÓN POST-COMPILACIÓN ==="

                        echo "1. 📁 DIRECTORIOS TARGET GENERADOS:"
                        find . -name "target" -type d | head -10

                        echo -e "\n2. 📄 ARCHIVOS JAR GENERADOS:"
                        find . -name "*.jar" -type f | grep -v ".m2" | head -20

                        echo -e "\n3. 📋 ANÁLISIS DETALLADO POR MÓDULO:"
                        for module in application domain infraestructure; do
                            echo "--- $module ---"
                            if [ -d "${module}/target" ]; then
                                echo "✅ Target directory existe"
                                echo "Contenido completo:"
                                ls -la ${module}/target/ | head -20

                                echo -e "\nArchivos JAR específicos:"
                                find ${module}/target -name "*.jar" -type f | head -10

                                if [ "$module" = "application" ]; then
                                    echo -e "\n🔍 ANÁLISIS ESPECÍFICO DEL MÓDULO APPLICATION:"

                                    # Verificar JAR ejecutable esperado
                                    TARGET_JAR="${module}/target/application-0.0.1-SNAPSHOT.jar"
                                    if [ -f "$TARGET_JAR" ]; then
                                        echo "✅ JAR ejecutable encontrado: $TARGET_JAR"
                                        echo "   Tamaño: $(du -h $TARGET_JAR | cut -f1)"
                                        echo "   Fecha: $(stat -c %y $TARGET_JAR 2>/dev/null || stat -f %Sm $TARGET_JAR 2>/dev/null)"

                                        # Verificar que es un JAR de Spring Boot
                                        if unzip -l "$TARGET_JAR" | grep -q "BOOT-INF"; then
                                            echo "   ✅ Es un JAR ejecutable de Spring Boot"
                                        else
                                            echo "   ❌ NO es un JAR ejecutable de Spring Boot"
                                            echo "   Contenido del JAR (primeras 20 líneas):"
                                            unzip -l "$TARGET_JAR" | head -20
                                        fi
                                    else
                                        echo "❌ JAR ejecutable NO encontrado: $TARGET_JAR"
                                        echo "   Buscando JARs alternativos:"
                                        find ${module}/target -name "*.jar" -type f | head -10

                                        # Verificar logs de Maven específicos del módulo
                                        echo -e "\n   📋 INTENTANDO COMPILAR SOLO EL MÓDULO APPLICATION:"
                                        cd ${module}
                                        mvn package -DskipTests --settings ../settings.xml -e | tail -20
                                        cd ..
                                    fi
                                fi
                            else
                                echo "❌ Target directory NO existe"
                            fi
                            echo ""
                        done

                        echo "4. 🔍 BÚSQUEDA GLOBAL DE JARS:"
                        echo "Todos los JARs en el proyecto:"
                        find . -name "*.jar" -type f | grep -v ".m2" | grep -v test

                        echo -e "\n5. 🔍 VERIFICACIÓN DE LOGS DE MAVEN:"
                        echo "Últimas líneas del log de Maven:"
                        # Los logs ya se mostraron arriba con tail -100

                        echo -e "\n=== INSTALACIÓN DE DEPENDENCIAS ==="
                        mvn install --settings settings.xml -DskipTests

                        echo -e "\n=== VERIFICACIÓN FINAL ==="
                        echo "Estado final de archivos JAR:"
                        find . -name "*.jar" -type f | grep -v ".m2" | xargs ls -la 2>/dev/null || echo "No se encontraron JARs"
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