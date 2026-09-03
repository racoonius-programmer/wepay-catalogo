# Catalogo (backend)

Proyecto Spring Boot simple que expone un catálogo de productos vía REST.

**Prerequisitos:**
- Java 17+ instalado
- Docker (opcional)
- `mvnw` (wrapper incluido)

**1) Compilar**

- Desde la carpeta del proyecto abre la terminal y ejecuta:
```bash
.\mvnw -DskipTests clean package
```

**2) Ejecutar en tu red local**

Sigue estos pasos sencillos para que otra persona de tu red pueda ver los productos:

- Ejecuta el archivo empaquetado (el programa) con estos ajustes para escuchar desde cualquier IP y usar el puerto 8090.

PowerShell puede interpretar mal los argumentos con `-D`. Usa una de estas opciones:

Opción A (recomendada en PowerShell): pasar las propiedades antes del goal del wrapper:
```powershell
.\mvnw -Dserver.address=0.0.0.0 -Dserver.port=8090 spring-boot:run
```

Opción B (ejecutar el JAR desde PowerShell usando `--%` para evitar el parsing de PowerShell):
```powershell
java --% -Dserver.address=0.0.0.0 -Dserver.port=8090 -jar target\\catalogo-0.0.1-SNAPSHOT.jar
```

Opción C (usar cmd.exe):
```cmd
cmd /c "java -Dserver.address=0.0.0.0 -Dserver.port=8090 -jar target\\catalogo-0.0.0.1-SNAPSHOT.jar"
```

Esto hace que el servidor escuche en todas las interfaces de red y en el puerto `8090`.

**3) Averiguar la IP de tu equipo (Windows)**

- Abre PowerShell y escribe:
```powershell
ipconfig
```
- Busca la `IPv4` asociada a la red a la que están conectados los otros equipos (ej: `192.168.1.42`).

**4) Ver los productos desde otro equipo**

- Desde otra computadora en la misma red, abre la terminal y ejecuta:
```bash
curl http://<IP_DE_TU_PC>:8090/api/products
```
Por ejemplo:
```bash
curl http://192.168.1.42:8090/api/products
```

- En PowerShell puedes usar:
```powershell
Invoke-RestMethod -Uri http://192.168.1.42:8090/api/products
```

**Probar con Postman**

- Abre Postman y crea una nueva request `GET`.
- URL local (misma máquina): `http://localhost:8090/api/products`.
- URL desde otra máquina: `http://<IP_DE_TU_PC>:8090/api/products` (ej. `http://192.168.1.42:8090/api/products`).
- Haz clic en `Send`. Debes recibir un `200 OK` con el JSON de productos.

Si necesitas ver cabeceras o el body en formato legible, en Postman revisa la pestaña `Body` y selecciona `Pretty` -> `JSON`.

**Si el puerto aparece en uso**

- Comprueba qué proceso está usando el puerto `8090` (PowerShell):
```powershell
Get-NetTCPConnection -LocalPort 8090 | Format-Table -AutoSize
```
- Obtén el PID y detén el proceso (si es seguro hacerlo):
```powershell
$pid = (Get-NetTCPConnection -LocalPort 8090).OwningProcess
Get-Process -Id $pid
Stop-Process -Id $pid -Force
```
- Alternativa rápida: ejecutar la app en otro puerto (ej. 8091) usando `--%` en PowerShell:
```powershell
java --% -Dserver.address=0.0.0.0 -Dserver.port=8091 -jar target\\catalogo-0.0.1-SNAPSHOT.jar
```
o con el wrapper:
```powershell
.\mvnw '-Dserver.port=8091' spring-boot:run
```

Nota: detén solo procesos que reconozcas; si no estás seguro, cambia el puerto.

**5) Si no funciona desde otra máquina**

- Revisa que tu firewall permita conexiones al puerto `8090`. En Windows puedes añadir una regla así:
```powershell
New-NetFirewallRule -DisplayName "Catalogo Inbound" -Direction Inbound -LocalPort 8090 -Protocol TCP -Action Allow
```

**6) Usar Docker (opcional)**

- Construir la imagen:
```bash
docker build -t catalogo:latest .
```
- Ejecutar el contenedor y exponer el puerto `8090`:
```bash
docker run --rm -p 8090:8090 catalogo:latest
```

Luego accede con `http://<IP_DE_TU_PC>:8090/api/products`.

**Endpoints principales** (base: http://<IP_HOST>:8081)
- `GET /api/products` — lista todos los productos
- `GET /api/products/{id}` — obtiene producto por id
- `POST /api/products` — crea un producto (JSON)
- `PUT /api/products/{id}` — actualiza un producto
- `DELETE /api/products/{id}` — borra un producto

**Carga del catálogo de ejemplo**

El proyecto incluye `src/main/resources/catalogo-gamer.json`. `ProductService` carga automáticamente ese archivo al iniciar y crea los productos en memoria.

Si quieres reproducir manualmente la carga usando PowerShell:
```powershell
Get-Content src\main\resources\catalogo-gamer.json | ConvertFrom-Json | ForEach-Object { Invoke-RestMethod -Uri http://localhost:8081/api/products -Method Post -Body ($_ | ConvertTo-Json) -ContentType 'application/json' }
```

**Notas**
- La implementación actual guarda los productos en memoria (no persistente). Para producción, sustituir `ProductService` por un repositorio con base de datos.
- Si ves caracteres extraños en los textos (ej. `mecÃ¡nico`), asegúrate de que la terminal y las herramientas usen UTF-8; puedes añadir `spring.http.encoding.charset=UTF-8` en `application.properties` si es necesario.


**Para ejecutarlo desde AWS EC2**
## 1) Despliegue y ejecución en AWS EC2 (Amazon Linux 2023)

Sigue estos pasos para desplegar el backend en una instancia EC2 de AWS desde cero:

### Paso 1: Instalar dependencias en el servidor EC2
Conéctate por SSH a la instancia e instala Git y Java:
```bash
sudo dnf update -y
sudo dnf install git java-21-amazon-corretto-devel -y
```

### Paso 2: Clonar el repo y asignarle permisos al mvnw
```bash
git clone [https://github.com/tu-usuario/wepay-catalogo.git](https://github.com/tu-usuario/wepay-catalogo.git)
cd wepay-catalogo
chmod +x mvnw
```
### Paso 3: Iniciar el servidor en la ip de la instancia
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.address=0.0.0.0 --server.port=8090"
```

### Paso 4: Habilitar acceso en el Security Group de AWS
Entra a la consola de AWS EC2 y selecciona tu instancia.

En la pestaña Security, haz clic en el Security Group de la instancia.

Haz clic en Edit inbound rules (Editar reglas de entrada).

Agrega la siguiente regla:

Tipo: Custom TCP
Rango de puertos: 8090
Origen: Anywhere-IPv4 (0.0.0.0/0)
Guarda la regla.