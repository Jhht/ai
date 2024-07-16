# PYR - Proyecto Demo de preguntas y respuestas

El proyecto ofrece una serie de endpoints para interactuar con servicios de pregunta y respuestas.
Demostración para Spring Boot que utiliza varias tecnologías, incluyendo WebFlux, ElasticSearch, OpenAI, y manipulación de PDF.

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalados los siguientes programas en tu máquina:

- JDK 17 o superior
- Maven 3.6.3 o superior

## Instalación

Clonar repositorio de bitbucket

- Contruir -> mvn clean install
- Ejecutar -> mvn spring-boot:run

# Endpoints Disponibles

## `/ask` - POST

Envía una pregunta y recibe una respuesta basada en el contenido almacenado.

- **URL:** `/ask`
- **Método:** `POST`
- **Request Body:**
  ```json
  {
    "question": "Tu pregunta aquí"
  } 
- **Request Response:**
  ```json
  {
   "Respuesta generada por OpenAI"
  }

## `/ask/stream` - POST

Envía una pregunta y recibe una respuesta basada en el contenido almacenado devolviendo 
la respuesta en un flujo palabra por palabra.

- **URL:** `/ask/stream`
- **Método:** `POST`
- **Request Body:**
  ```json
  {
    "question": "Tu pregunta aquí"
  } 
- **Request Response:**
  ```json
  {
   "Respuesta generada por OpenAI"
  }

## `/conversations/start` - POST

Inicia una nueva conversación o continúa una existente.

- **URL:** `/conversations/start`
- **Método:** `POST`
- **Request Body:**
  ```json
  {
    "question": "Tu pregunta aquí"
  } 
- **Request Response:**
  ```json
  {
   "Respuesta generada por OpenAI"
  }

## `/cancel/thread/{threadId}` - DELETE

Cancela un hilo de conversación a partir de su id.

- **URL:** `/cancel/thread/{threadId}`
- **Método:** `DELETE`
- **Request Response:** `200 ok`

## `/documents/upload` - POST

Carga un documento PDF y lo procesa, almacenando su contenido.
El path del documento esta harcoded en el código

- **URL:** `/documents/upload`
- **Método:** `POST`
- **Request Response:** `200 ok`

## `/documents/{id}` - DELETE

Elimina un documento a partir de su id.

- **URL:** `/documents/{id}`
- **Método:** `DELETE`
- **Request Response:** `200 ok`
