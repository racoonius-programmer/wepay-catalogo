# Catalogo (backend)

Proyecto Spring Boot que expone un catálogo de productos vía REST y puede ejecutarse tanto localmente como en Docker o en una instancia EC2 de AWS.

## Requisitos

- Java 25 instalado
- Maven wrapper incluido (`mvnw`)
- Docker Desktop o Docker Engine instalado
- Git (opcional para clonar desde repositorio)

## 1) Instalar Docker en Windows

Si vas a usar Docker en tu PC Windows:

1. Descarga Docker Desktop desde: https://www.docker.com/products/docker-desktop/
2. Instálalo y reinicia el equipo.
3. Abre PowerShell y valida:

```powershell
docker --version
```

Si no aparece error, Docker quedó instalado correctamente.

## 2) Compilar la aplicación sin Docker

Desde la carpeta del proyecto ejecuta:

```powershell
.\mvnw -DskipTests clean package
```

Esto genera el JAR en `target`.

## 3) Ejecutar la app en la red local

La app debe escuchar en todas las interfaces (`0.0.0.0`) y en el puerto `8090`.

### Opción A: ejecutar con Maven

```powershell
.\mvnw -Dserver.address=0.0.0.0 -Dserver.port=8090 spring-boot:run
```

### Opción B: ejecutar el JAR

```powershell
java --% -Dserver.address=0.0.0.0 -Dserver.port=8090 -jar target\catalogo-0.0.1-SNAPSHOT.jar
```

### Opción C: usar cmd.exe

```cmd
cmd /c "java -Dserver.address=0.0.0.0 -Dserver.port=8090 -jar target\catalogo-0.0.1-SNAPSHOT.jar"
```

## 4) Ver la IP de tu equipo y acceder desde otra PC

En Windows:

```powershell
ipconfig
```

Busca la IPv4 de la red local, por ejemplo:

```text
192.168.1.42
```

Desde otra computadora de la misma red, prueba:

```bash
curl http://192.168.1.42:8090/api/products
```

o en PowerShell:

```powershell
Invoke-RestMethod -Uri http://192.168.1.42:8090/api/products
```

## 5) Probar en Postman

- URL local: `http://localhost:8090/api/products`
- URL desde otra PC: `http://<IP_DE_TU_PC>:8090/api/products`
- Método: `GET`
- Debes recibir un `200 OK` y un JSON con los productos.

## 6) Si el puerto 8090 está ocupado

Revisa qué proceso lo usa:

```powershell
Get-NetTCPConnection -LocalPort 8090 | Format-Table -AutoSize
```

Si es seguro, detén el proceso:

```powershell
$pid = (Get-NetTCPConnection -LocalPort 8090).OwningProcess
Get-Process -Id $pid
Stop-Process -Id $pid -Force
```

También puedes cambiar el puerto a 8091:

```powershell
java --% -Dserver.address=0.0.0.0 -Dserver.port=8091 -jar target\catalogo-0.0.1-SNAPSHOT.jar
```

## 7) Si no funciona desde otra máquina

Revisa el firewall de Windows:

```powershell
New-NetFirewallRule -DisplayName "Catalogo Inbound" -Direction Inbound -LocalPort 8090 -Protocol TCP -Action Allow
```

Y confirma que la IP que usas es la correcta del equipo que ejecuta la app.

## 8) Ejecutar con Docker localmente

### Construir la imagen

```bash
docker build -t sh1r8/catalogo:latest .
```

### Ejecutar el contenedor

```bash
docker run --rm -p 8090:8090 sh1r8/catalogo:latest
```

### Probarlo

```bash
curl http://localhost:8090/api/products
```

## 9) Subir la imagen a Docker Hub

Primero inicia sesión:

```bash
docker login
```

Luego sube la imagen:

```bash
docker push sh1r8/catalogo:latest
```

Si quieres, puedes usar una versión etiquetada:

```bash
docker tag sh1r8/catalogo:latest sh1r8/catalogo:v1
docker push sh1r8/catalogo:v1
```

## 10) Ejecutar la imagen en una EC2 de AWS

Esto permite que la app esté expuesta con la IP pública de la instancia EC2.

### 10.1) Instalar Docker en la EC2

En Amazon Linux 2023:

```bash
sudo dnf update -y
sudo dnf install -y docker git
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
newgrp docker
```

Verifica:

```bash
docker --version
```

### 10.2) Descargar la imagen desde Docker Hub

```bash
docker pull sh1r8/catalogo:latest
```

### 10.3) Ejecutar el contenedor

```bash
docker run -d --name catalogo -p 8090:8090 sh1r8/catalogo:latest
```

### 10.4) Verificar acceso local en la EC2

```bash
curl http://localhost:8090/api/products
```

### 10.5) Abrir el puerto en el Security Group de AWS

En la EC2:

- Ve a la instancia
- Entra a `Security Groups`
- Edita las reglas de entrada
- Agrega:
  - Tipo: `Custom TCP`
  - Puerto: `8090`
  - Origen: `0.0.0.0/0`

Esto permite acceso desde Internet.

### 10.6) Probar con la IP pública de la EC2

Obtén la IP pública de la instancia y luego prueba:

```bash
curl http://<IP_PUBLICA_EC2>:8090/api/products
```

Ejemplo:

```bash
curl http://18.216.123.45:8090/api/products
```

## 11) Endpoints principales

Base URL local o pública:

```text
http://<IP_HOST>:8090
```

- `GET /api/products` — lista todos los productos
- `GET /api/products/{id}` — obtiene un producto por id
- `POST /api/products` — crea un producto
- `PUT /api/products/{id}` — actualiza un producto
- `DELETE /api/products/{id}` — elimina un producto

## 12) Carga del catálogo de ejemplo

El proyecto incluye `src/main/resources/catalogo-gamer.json` y `ProductService` lo carga automáticamente al iniciar.

Si quieres reenviar manualmente los productos con PowerShell:

```powershell
Get-Content src\main\resources\catalogo-gamer.json | ConvertFrom-Json | ForEach-Object { Invoke-RestMethod -Uri http://localhost:8090/api/products -Method Post -Body ($_ | ConvertTo-Json) -ContentType 'application/json' }
```

## 13) Notas

- La implementación actual guarda los productos en memoria; no es persistente.
- Para producción, conviene reemplazar `ProductService` por un repositorio con base de datos.
- Si ves caracteres extraños, revisa que la terminal use UTF-8.
- La app está configurada para Java 25 y para escuchar en `0.0.0.0` en el puerto `8090`.