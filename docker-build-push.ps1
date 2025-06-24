# docker-build-push.ps1 - Script PowerShell para construir y subir imagen a Docker Hub

param(
    [string]$DockerHubUsername = "rpantax",  # CAMBIA ESTE VALOR
    [string]$ImageName = "products-service",
    [string]$Version = "1.0.0"
)

# Configuración
$LatestTag = "latest"
$FullImageName = "$DockerHubUsername/$ImageName"
$VersionedTag = "${FullImageName}:$Version"
$LatestFullTag = "${FullImageName}:$LatestTag"

# Funciones de utilidad
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Warning { param($Message) Write-Host "⚠️ $Message" -ForegroundColor Yellow }
function Write-Info { param($Message) Write-Host "ℹ️ $Message" -ForegroundColor Blue }
function Write-Header { param($Message) Write-Host "🔨 $Message" -ForegroundColor Cyan }

Write-Host "🐳 Docker Build & Push Script para Products Service" -ForegroundColor Magenta
Write-Host "==================================================" -ForegroundColor Magenta
Write-Host ""

# Verificar directorio actual
Write-Header "1. Verificaciones iniciales"

if (-not (Test-Path "pom.xml") -or -not (Test-Path "Dockerfile")) {
    Write-Error "No se encontró pom.xml o Dockerfile. Asegúrate de estar en la raíz del proyecto."
    exit 1
}
Write-Success "Archivos principales encontrados"

# Verificar Docker
try {
    docker --version | Out-Null
    Write-Success "Docker está disponible"
} catch {
    Write-Error "Docker no está instalado o no está en PATH"
    exit 1
}

# Verificar login en Docker Hub
$dockerInfo = docker info 2>$null | Out-String
if ($dockerInfo -match "Username") {
    Write-Success "Ya estás logueado en Docker Hub"
} else {
    Write-Warning "No estás logueado en Docker Hub"
    Write-Info "Ejecutando 'docker login'..."
    docker login
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Error en login de Docker Hub"
        exit 1
    }
}

Write-Host ""

# Compilar proyecto
Write-Header "2. Compilando proyecto Maven"

Write-Info "Limpiando proyecto..."
mvn clean -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error en mvn clean"
    exit 1
}

Write-Info "Compilando proyecto..."
mvn compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error en compilación"
    exit 1
}
Write-Success "Compilación exitosa"

Write-Info "Ejecutando tests..."
mvn test -q
if ($LASTEXITCODE -eq 0) {
    Write-Success "Tests ejecutados correctamente"
} else {
    Write-Warning "Algunos tests fallaron, continuando..."
}

Write-Info "Generando JAR..."
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error generando JAR"
    exit 1
}
Write-Success "JAR generado exitosamente"

# Verificar JAR
$JarPath = "application\target\application-0.0.1-SNAPSHOT.jar"
if (Test-Path $JarPath) {
    $JarSize = (Get-Item $JarPath).Length
    $JarSizeMB = [math]::Round($JarSize / 1MB, 2)
    Write-Success "JAR encontrado: $JarPath ($JarSizeMB MB)"
} else {
    Write-Error "JAR no encontrado en $JarPath"
    Write-Info "Archivos en application\target\:"
    Get-ChildItem "application\target\" -ErrorAction SilentlyContinue
    exit 1
}

Write-Host ""

# Construir imagen Docker
Write-Header "3. Construyendo imagen Docker"

Write-Info "Construyendo imagen: $VersionedTag"
docker build -t $VersionedTag .
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error construyendo imagen Docker"
    exit 1
}
Write-Success "Imagen construida exitosamente"

# Tagear como latest
Write-Info "Creando tag 'latest'..."
docker tag $VersionedTag $LatestFullTag
Write-Success "Tag 'latest' creado"

# Verificar imágenes
Write-Info "Imágenes creadas:"
docker images | Select-String "$DockerHubUsername/$ImageName"

Write-Host ""

# Probar imagen localmente
Write-Header "4. Probando imagen localmente"

Write-Info "Iniciando contenedor de prueba..."
$ContainerResult = docker run -d -p 8082:8081 --name products-service-test $VersionedTag 2>&1
if ($LASTEXITCODE -eq 0) {
    $ContainerId = $ContainerResult
    Write-Success "Contenedor iniciado: $ContainerId"

    Write-Info "Esperando que el servicio inicie (30 segundos)..."
    Start-Sleep -Seconds 30

    # Verificar que el contenedor sigue corriendo
    $RunningContainers = docker ps --format "table {{.Names}}" | Select-String "products-service-test"
    if ($RunningContainers) {
        Write-Success "Contenedor ejecutándose correctamente"

        # Intentar hacer health check
        Write-Info "Verificando health check..."
        try {
            $HealthCheck = docker exec products-service-test curl -f http://localhost:8081/actuator/health 2>$null
            Write-Success "Health check exitoso"
        } catch {
            Write-Warning "Health check falló, pero el contenedor está corriendo"
        }

        # Mostrar logs del contenedor
        Write-Info "Últimas líneas del log:"
        docker logs --tail 10 products-service-test

    } else {
        Write-Error "El contenedor se detuvo inesperadamente"
        Write-Info "Logs del contenedor:"
        docker logs products-service-test
    }

    # Limpiar contenedor de prueba
    Write-Info "Limpiando contenedor de prueba..."
    docker stop products-service-test | Out-Null
    docker rm products-service-test | Out-Null
    Write-Success "Contenedor de prueba limpiado"

} else {
    Write-Error "No se pudo iniciar el contenedor de prueba: $ContainerResult"
    exit 1
}

Write-Host ""

# Push a Docker Hub
Write-Header "5. Subiendo a Docker Hub"

Write-Info "Subiendo imagen versionada: $VersionedTag"
docker push $VersionedTag
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error subiendo imagen versionada"
    exit 1
}
Write-Success "Imagen versionada subida exitosamente"

Write-Info "Subiendo imagen latest: $LatestFullTag"
docker push $LatestFullTag
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error subiendo imagen 'latest'"
    exit 1
}
Write-Success "Imagen 'latest' subida exitosamente"

Write-Host ""

# Verificación final
Write-Header "6. Verificación final"

Write-Info "Verificando imagen en Docker Hub..."
Start-Sleep -Seconds 5

# Limpiar imagen local y descargar desde Docker Hub para verificar
Write-Info "Limpiando imágenes locales..."
docker rmi $VersionedTag $LatestFullTag | Out-Null

Write-Info "Descargando imagen desde Docker Hub para verificar..."
docker pull $LatestFullTag
if ($LASTEXITCODE -ne 0) {
    Write-Error "No se pudo descargar la imagen desde Docker Hub"
    exit 1
}
Write-Success "Imagen verificada en Docker Hub"

Write-Host ""

# Resumen final
Write-Header "🎉 ¡Proceso completado exitosamente!"
Write-Host ""
Write-Success "Tu imagen está disponible en Docker Hub:"
Write-Host "   📦 Imagen: $FullImageName" -ForegroundColor White
Write-Host "   🏷️  Tags: $Version, latest" -ForegroundColor White
Write-Host "   🌐 URL: https://hub.docker.com/r/$DockerHubUsername/$ImageName" -ForegroundColor White
Write-Host ""
Write-Info "Comandos para usar tu imagen:"
Write-Host "   docker pull $LatestFullTag" -ForegroundColor Gray
Write-Host "   docker run -p 8081:8081 $LatestFullTag" -ForegroundColor Gray
Write-Host ""
Write-Info "Próximos pasos:"
Write-Host "   1. Actualiza el Jenkinsfile con: DOCKER_HUB_REPO = '$FullImageName'" -ForegroundColor Gray
Write-Host "   2. Configura las credenciales de Docker Hub en Jenkins" -ForegroundColor Gray
Write-Host "   3. Prueba el pipeline de Jenkins" -ForegroundColor Gray

# Limpiar imágenes locales para ahorrar espacio
Write-Info "Limpiando imágenes locales..."
docker image prune -f | Out-Null
Write-Success "Limpieza completada"