# Luis Fornaris y Jose Narvaez
# 🛠️ Configuración del Proyecto con Docker

Este proyecto utiliza **Docker** y **Docker Compose** para gestionar los microservicios y sus bases de datos.

## 📌 **Requisitos Previos**
Antes de comenzar, asegúrate de tener instalado en tu sistema:
- [Git](https://git-scm.com/downloads)
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## 🚀 **Pasos para Configurar y Ejecutar el Proyecto**

### 1️⃣ **Clonar el Repositorio**
```bash
# Clonar el repositorio desde GitHub
git clone https://github.com/JosCariDe/spring-microservices.git

# Ir al directorio del proyecto
cd spring-microservices
```

### 2️⃣ **Levantar los Contenedores con Docker Compose**
```bash
# Apagar cualquier instancia previa (opcional, por si acaso)
docker-compose down

# Construir y levantar los servicios en segundo plano
docker-compose up --build -d
```
📌 **Nota:** El flag `--build` se usa para asegurarse de que se reconstruyan las imágenes con los cambios más recientes.

### 3️⃣ **Verificar que los Contenedores están Corriendo**
```bash
docker ps
```
Si todo está bien, deberías ver los contenedores ejecutándose.

### 4️⃣ **Ver los Logs de los Servicios**
Si hay problemas, revisa los logs de los contenedores con:
```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico (ejemplo: order-service)
docker-compose logs -f order-service
```

### 5️⃣ **Acceder a las Aplicaciones**
Cada microservicio se ejecuta en un puerto diferente. Asegúrate de que los puertos especificados en `docker-compose.yml` estén disponibles en tu máquina.

- **Eureka Client:** [http://localhost:8080](http://localhost:8080)
- **Discovery Server:** [http://localhost:8083](http://localhost:8080)
- **Eureka Server:** [http://localhost:8061](http://localhost:8080)
- **Order Service:** [http://localhost:8786](http://localhost:8080)
- **Inventory Service:** [http://localhost:8381](http://localhost:8181)
- **Payment Service:** [http://localhost:8180](http://localhost:8182)
- **Product Service:** [http://localhost:8380](http://localhost:8280)

### 6️⃣ **Detener los Contenedores**
Para detener los contenedores sin eliminarlos:
```bash
docker-compose stop
```
Para detener y eliminar los contenedores, redes y volúmenes:
```bash
docker-compose down -v
```

---

## ❓ **Problemas Comunes y Soluciones**
### 🔴 **Docker no puede asignar puertos**
Si algún servicio falla porque el puerto ya está en uso, verifica qué procesos lo están ocupando:
```bash
sudo lsof -i :8080  # Reemplaza 8080 por el puerto que necesites verificar
```
Para liberar el puerto:
```bash
sudo kill -9 <PID>
```

### 🔴 **Error de conexión a la base de datos**
Si un servicio no puede conectarse a su base de datos, revisa:
- Que los contenedores de la base de datos están corriendo (`docker ps`)
- Que las credenciales en `docker-compose.yml` son correctas
- Intenta reiniciar los servicios con:
  ```bash
  docker-compose down && docker-compose up --build -d
  ```

---



