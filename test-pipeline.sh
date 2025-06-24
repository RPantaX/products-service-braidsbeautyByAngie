#!/bin/bash

# test-pipeline.sh - Script para verificar el pipeline de Jenkins

set -e

echo "🧪 Verificando Pipeline de Products Service"
echo "=========================================="

# Variables
JENKINS_URL="http://54.209.183.200:8080"
JOB_NAME="products-service-braids"
DOCKER_HUB_REPO="rpantax/products-service"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Funciones de utilidad
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_info() {
    echo -e "ℹ️ $1"
}

# Verificar prerequisitos locales
echo "1. Verificando prerequisitos locales..."

if command -v git &> /dev/null; then
    print_success "Git está instalado"
else
    print_error "Git no está instalado"
    exit 1
fi

if command -v mvn &> /dev/null; then
    print_success "Maven está instalado"
else
    print_error "Maven no está instalado"
fi

if command -v docker &> /dev/null; then
    print_success "Docker está instalado"
else
    print_error "Docker no está instalado"
fi

echo ""

# Verificar estructura del proyecto
echo "2. Verificando estructura del proyecto..."

if [ -f "pom.xml" ]; then
    print_success "POM principal encontrado"
else
    print_error "POM principal no encontrado"
    exit 1
fi

if [ -f "Jenkinsfile" ]; then
    print_success "Jenkinsfile encontrado"
else
    print_error "Jenkinsfile no encontrado"
    exit 1
fi

if [ -f "Dockerfile" ]; then
    print_success "Dockerfile encontrado"
else
    print_error "Dockerfile no encontrado"
    exit 1
fi

if [ -d "application" ] && [ -d "domain" ] && [ -d "infraestructure" ]; then
    print_success "Estructura modular correcta"
else
    print_error "Estructura modular incorrecta"
    exit 1
fi

echo ""

# Verificar compilación local
echo "3. Verificando compilación local..."

print_info "Compilando proyecto..."
if mvn clean compile -q; then
    print_success "Compilación exitosa"
else
    print_error "Error en compilación"
    exit 1
fi

print_info "Ejecutando tests..."
if mvn test -q; then
    print_success "Tests ejecutados correctamente"
else
    print_warning "Algunos tests fallaron, verifica logs"
fi

print_info "Generando JAR..."
if mvn package -DskipTests -q; then
    print_success "JAR generado correctamente"
    if [ -f "application/target/application-0.0.1-SNAPSHOT.jar" ]; then
        print_success "JAR encontrado en la ubicación correcta"
    else
        print_error "JAR no encontrado en application/target/"
    fi
else
    print_error "Error generando JAR"
    exit 1
fi

echo ""

# Verificar Docker build local
echo "4. Verificando Docker build local..."

print_info "Construyendo imagen Docker..."
if docker build -t products-service-test .; then
    print_success "Imagen Docker construida correctamente"
else
    print_error "Error construyendo imagen Docker"
    exit 1
fi

print_info "Verificando imagen Docker..."
if docker images | grep -q "products-service-test"; then
    print_success "Imagen Docker verificada"
else
    print_error "Imagen Docker no encontrada"
fi

# Limpiar imagen de test
docker rmi products-service-test &> /dev/null || true

echo ""

# Verificar conectividad con Jenkins
echo "5. Verificando conectividad con Jenkins..."

if curl -s "$JENKINS_URL" > /dev/null; then
    print_success "Jenkins es accesible"
else
    print_error "No se puede conectar a Jenkins en $JENKINS_URL"
    print_info "Verifica que Jenkins esté ejecutándose y la URL sea correcta"
fi

echo ""

# Verificar repositorio Git
echo "6. Verificando repositorio Git..."

CURRENT_BRANCH=$(git branch --show-current)
print_info "Rama actual: $CURRENT_BRANCH"

if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "develop" ]; then
    print_success "Estás en una rama válida para el pipeline"
else
    print_warning "Estás en la rama '$CURRENT_BRANCH', el pipeline solo se ejecuta en 'main' y 'develop'"
fi

REMOTE_URL=$(git config --get remote.origin.url)
print_info "URL remota: $REMOTE_URL"

if git status --porcelain | grep -q .; then
    print_warning "Tienes cambios sin commitear"
else
    print_success "Working directory limpio"
fi

echo ""

# Verificar archivos de configuración
echo "7. Verificando archivos de configuración..."

if grep -q "tu-usuario-dockerhub" Jenkinsfile; then
    print_warning "Recuerda actualizar 'tu-usuario-dockerhub' en Jenkinsfile"
fi

if grep -q "tu-usuario" Jenkinsfile; then
    print_warning "Recuerda actualizar 'tu-usuario' en Jenkinsfile"
fi

if grep -q "tu-jenkins-ip" Jenkinsfile; then
    print_warning "Recuerda actualizar 'tu-jenkins-ip' en Jenkinsfile"
fi

echo ""

# Instrucciones finales
echo "8. Próximos pasos:"
echo "==================="
print_info "1. Actualiza las variables en Jenkinsfile:"
print_info "   - DOCKER_HUB_REPO"
print_info "   - GitHub URLs"
print_info "   - Jenkins IPs"
print_info ""
print_info "2. Configura credenciales en Jenkins:"
print_info "   - github-token"
print_info "   - dockerhub-credentials"
print_info ""
print_info "3. Crea el job en Jenkins y activa webhook en GitHub"
print_info ""
print_info "4. Haz un push a develop o main para probar:"
print_info "   git add ."
print_info "   git commit -m 'Setup Jenkins pipeline'"
print_info "   git push origin $CURRENT_BRANCH"

echo ""
print_success "Verificación completada! 🚀"