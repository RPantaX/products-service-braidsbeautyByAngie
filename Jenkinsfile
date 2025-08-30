pipeline {
    agent any

    environment {
        DOCKER_HUB_REPO = 'rpantax/products-service'
        DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        // Repositorio Maven compartido - CORREGIDO
        MAVEN_OPTS = "-Dmaven.repo.local=${WORKSPACE}/.m2/repository"
        GITHUB_USERNAME = 'RPantaX'
        GITHUB_TOKEN = credentials('github-token-2')
        CURRENT_BRANCH = "${env.BRANCH_NAME ?: 'main'}"
    }

    tools {
        maven 'maven4.0.0'
    }

    stages {
        stage('Clone Core Service') {
            steps {
                echo "Clonando core-service-braidsbeautyByAngie..."
                dir('core-service') {
                    git branch: 'main',
                        url: 'https://github.com/RPantaX/core-service-braidsbeautyByAngie.git',
                        credentialsId: 'github-token'
                }
            }
        }

        stage('Install Core Service') {
            steps {
                dir('core-service') {
                    echo "Instalando core-service en el repositorio local..."
                    // Usar el repositorio Maven compartido
                    sh "mvn clean install -DskipTests -Dmaven.repo.local=${WORKSPACE}/.m2/repository"
                }
            }
        }

        stage('Clone Product Service') {
            steps {
                echo "Clonando products-service-braidsbeautyByAngie..."
                dir('products-service') {
                    git branch: 'developer',
                        url: 'https://github.com/RPantaX/products-service-braidsbeautyByAngie.git',
                        credentialsId: 'github-token'
                }

                script {
                    dir('products-service') {
                        env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                        env.CURRENT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                        env.DOCKER_IMAGE_TAG = "${BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    }
                    echo "Building from branch: ${env.CURRENT_BRANCH}"
                    echo "Git commit: ${env.GIT_COMMIT}"
                    echo "Docker tag: ${env.DOCKER_IMAGE_TAG}"
                }
            }
        }

        stage('Build Product Service') {
            steps {
                dir('products-service') {
                    echo "Compilando products-service..."
                    // Usar el mismo repositorio Maven compartido
                    sh "mvn clean package -DskipTests -Dmaven.repo.local=${WORKSPACE}/.m2/repository"
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    // Asegurar que estamos en el directorio correcto
                    dir('products-service') {
                        def dockerImage = docker.build("${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}")
                        env.DOCKER_IMAGE_ID = dockerImage.id

                        dockerImage.tag("${env.CURRENT_BRANCH}-latest")
                        if (env.CURRENT_BRANCH == 'main') {
                            dockerImage.tag('latest')
                        }
                        echo "Docker image built successfully: ${DOCKER_HUB_REPO}:${DOCKER_IMAGE_TAG}"
                    }
                }
            }
        }

        stage('Docker Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'developer'
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
                        image.push("${env.CURRENT_BRANCH}-latest")

                        if (env.CURRENT_BRANCH == 'main') {
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
                if (env.CURRENT_BRANCH == 'main' || env.CURRENT_BRANCH == 'developer') {
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