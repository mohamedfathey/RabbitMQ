# RabbitMQ JSON Messaging with Spring Boot 🐰📬

Welcome to this awesome setup for **asynchronous communication** using **RabbitMQ** to send and receive JSON messages! 🎉 This project features two Spring Boot apps: a **Producer** (`service-two`) that sends `User` objects to a RabbitMQ queue, and a **Consumer** (`service-one`) that receives and logs them. It’s clean, focused, and ready to rock! 😎

## 📖 Table of Contents
- [What is RabbitMQ?](#what-is-rabbitmq)
- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Producer App (`service-two`)](#producer-app-service-two)
- [Consumer App (`service-one`)](#consumer-app-service-one)
- [Running the Project](#running-the-project)
- [Testing](#testing)
- [Concepts Explained](#concepts-explained)
- [Troubleshooting](#troubleshooting)

##  What is RabbitMQ?  
🐰 RabbitMQ is a **message broker** that powers **asynchronous communication**. Think of it as a post office: the Producer sends a message to a queue, RabbitMQ holds it, and the Consumer picks it up when ready. This decouples services, making your system scalable and resilient. 😊

- **Queue**: A FIFO buffer that stores messages.
- **Exchange**: Routes messages to queues based on routing keys. We use a **Topic Exchange** for flexible routing.
- **Producer**: Sends messages to an exchange.
- **Consumer**: Listens to a queue and processes messages.
- **Routing Key**: Determines which queue(s) a message goes to.

##  Project Overview  
🌟 This project demonstrates asynchronous JSON messaging with:
- **Producer App (`service-two`)**: Sends a `User` object (with `id`, `firstName`, `lastName`) to a RabbitMQ queue via an HTTP endpoint (`/api/v1/publish`).
- **Consumer App (`service-one`)**: Listens to the queue and logs received `User` objects.
- **RabbitMQ**: Manages the queue (`javaguides_json`), exchange (`javaguides_exchange`), and routing key (`javaguides_routing_json_key`).

The Producer’s HTTP endpoint is secured with an `X-API-Key` header to prevent unauthorized access. Messages are serialized to JSON using `Jackson2JsonMessageConverter`. 🔒

##  Prerequisites 
🛠️
- **Java 17** or later ☕
- **Maven** for dependency management 📦
- **Docker** (to run RabbitMQ) 🐳

##  Setup Instructions  
### 1. Install RabbitMQ
Run RabbitMQ using Docker:
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
- **Port 5672**: For AMQP (RabbitMQ’s protocol).
- **Port 15672**: For the management UI (access at `http://localhost:15672`, default credentials: `guest/guest`).

##  Producer App (`service-two`)  
 The Producer sends `User` objects to a RabbitMQ queue via an HTTP endpoint.📨

### Configuration
- **`application.properties`**:
  ```properties
  spring.application.name=service-two
  server.port=9992

  spring.rabbitmq.host=localhost
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest

  rabbitmq.queue.name=javaguides
  rabbitmq.queue.json.name=javaguides_json
  rabbitmq.exchange.name=javaguides_exchange
  rabbitmq.routing.name=javaguides_routing_key
  rabbitmq.routing.json.name=javaguides_routing_json_key

  management.endpoints.web.exposure.include=
  ```
- Connects to RabbitMQ (`localhost:5672`).
- Defines queues (`javaguides`, `javaguides_json`), exchange (`javaguides_exchange`), and routing keys. Only `javaguides_json` and `javaguides_routing_json_key` are used.
- Disables Actuator endpoints for security.

### Code Highlights
- **DTO**: `User` (with `id`, `firstName`, `lastName`).
- **Controller**: `MessageJsonController` exposes `/api/v1/publish` to send `User` objects.
- **Publisher**: `RabbitMQProducer` sends `User` objects to the `javaguides_json` queue via the exchange.
- **Config**: `RabbitMQConfig` sets up queues, exchange, bindings, and JSON converter.
- **Security**: `AuthFilter` requires `X-API-Key: my-secret-key` for the HTTP endpoint.

##  Consumer App (`service-one`)  
The Consumer listens to the RabbitMQ queue and logs `User` objects.📥

### Configuration
- **`application.properties`**:
  ```properties
  spring.application.name=service-one
  server.port=9991

  spring.rabbitmq.host=localhost
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest

  rabbitmq.queue.name=javaguides
  rabbitmq.queue.json.name=javaguides_json
  rabbitmq.exchange.name=javaguides_exchange
  rabbitmq.routing.name=javaguides_routing_key
  rabbitmq.routing.json.name=javaguides_routing_json_key

  management.endpoints.web.exposure.include=
  ```
- Same RabbitMQ setup as the Producer, focused on `javaguides_json` queue.

### Code Highlights
- **DTO**: `User` (identical to Producer’s).
- **Consumer**: `RabbitMQConsumer` uses `@RabbitListener` to consume `User` objects from `javaguides_json`.
- **Config**: `RabbitMQConfig` mirrors Producer’s setup for consistency.

## Running the Project  
1. Start **RabbitMQ**: 🏃
   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```
2. Start **Consumer** (`service-one`, `port: 9991`).
3. Start **Producer** (`service-two`, `port: 9992`).

##  Testing 
-  **Send a message**:
  ```bash
  curl -X POST http://localhost:9992/api/v1/publish -H "Content-Type: application/json" -H "X-API-Key: my-secret-key" -d '{"id": 1, "firstName": "John", "lastName": "Doe"}'
  ```
  - Response: `"Json Message Sent To RabbitMQ ....!"`
- **Check Consumer**:
  - Check the Consumer’s console logs. You should see: `Received message -> User{id=1, firstName='John', lastName='Doe'}`.
- **Test unauthorized access**:
  ```bash
  curl -X POST http://localhost:9992/api/v1/publish -H "Content-Type: application/json" -d '{"id": 1, "firstName": "John", "lastName": "Doe"}'
  ```
  - Expected: `403 Forbidden` with “Access denied: Invalid or missing API key”.

## Concepts Explained  
- **Asynchronous Communication**: The Producer sends `User` objects without waiting for the Consumer, unlike synchronous HTTP calls. RabbitMQ ensures delivery even if the Consumer is offline temporarily. 🚀
- **Topic Exchange**: Routes messages to queues based on routing keys. The `javaguides_routing_json_key` binds the `javaguides_json` queue to the `javaguides_exchange`. 📬
- **JSON Serialization**: `Jackson2JsonMessageConverter` converts `User` objects to JSON and back, enabling complex data transfer. 🗃️
- **Security**: The `X-API-Key` header secures the Producer’s endpoint. 🔐
- **Unused Config**: The `javaguides` queue and `javaguides_routing_key` are defined but unused in this setup. They’re included for potential future expansion.

## 🛠️ Troubleshooting  
- **RabbitMQ not connecting?** Ensure `localhost:5672` is accessible and credentials are `guest/guest`.
- **No messages received?** Verify the queue (`javaguides_json`), exchange (`javaguides_exchange`), and routing key (`javaguides_routing_json_key`) match in both apps, and the Consumer is running.
- **JSON errors?** Ensure `User` DTOs are identical in both apps (same fields and types).
- **Unauthorized errors?** Include `X-API-Key: my-secret-key` in requests to the Producer.

Enjoy your RabbitMQ JSON messaging! 🎉
