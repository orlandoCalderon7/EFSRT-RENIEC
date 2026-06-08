#  Proyecto Sechura: Integración de Componentes (RENIEC)
# RENIEC - Sede Sechura
### Sistema de Gestión de Trámites y Citas

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)
![Java](https://img.shields.io/badge/Java-25-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Maven](https://img.shields.io/badge/Maven-3.x-red)

Sistema web para la gestión de trámites documentales y reserva de citas en la
sede RENIEC de Sechura. Permite a los ciudadanos registrarse, iniciar trámites
(como renovación o duplicado de DNI) y reservar citas en horarios disponibles.

## Características

- Registro y autenticación de ciudadanos
- Gestión de trámites documentales (DNI primera vez, renovación, duplicado, etc.)
- Reserva de citas con selección de fecha y horario
- Generación de ticket digital por cita
- Sistema de notificaciones
- Panel de administración para gestores
- Verificación biofacial de usuarios

## Requisitos Previos

Antes de ejecutar el proyecto asegúrate de tener instalado:

- [Java JDK 25+](https://www.oracle.com/java/technologies/downloads/)
- [Maven 3.x+](https://maven.apache.org/download.cgi)
- [MySQL Server 8.0+](https://dev.mysql.com/downloads/mysql/)
- [MySQL Workbench](https://dev.mysql.com/downloads/workbench/)
- [VS Code](https://code.visualstudio.com/) o cualquier IDE Java

## Tecnologias Utilizadas

### Backend
- **Java 17**: Lenguaje principal.
- **Spring Boot 4.0.6**: Framework para la lógica de negocio y API REST.
- **Spring Data JPA**: Para el mapeo y gestión de la base de datos.
- **Maven**: Gestor de dependencias y construcción del proyecto.

### Base de Datos
- **MySQL**: Motor de base de datos relacional.
- **Connector J**: Driver oficial para la conexión Java-MySQL.

### Frontend
- **Estructura Integrada**: Componentes desarrollados para la interacción con el usuario.


### 1. Clonar el repositorio
 git clone https://github.com/orlandoCalderon7/EFSRT-RENIEC


##  Estructura del Proyecto

- `src/main/java`: Contiene los controladores, modelos y repositorios del Backend.
- `src/main/resources`: Archivos de configuración (como `application.properties`).
- `pom.xml`: Configuración de dependencias de Maven.
- `/frontend`: (Ubicación recomendada) Archivos de la interfaz de usuario.

## Configuración e Instalación

### Requisitos Previos
1. **JDK 17** instalado.
2. **MySQL Server** en ejecución.
3. **Maven** configurado.

### Ejecutar el Proyecto
mvn spring-boot:run

http://localhost:8080


