# Smart Doc Backend (SD-BE)

## Overview

**SD-BE** is the backend service for the **Smart Doc** module of Open Hospital — a digital patient documentation system designed to catalog and store medical documents in a structured and secure manner.

This service operates as part of the new **pluggable micro frontend architecture** for Open Hospital, where it provides specialized document management APIs while leveraging the existing OH-API/BE for authentication, authorization, and core hospital data proxying.

## Key Responsibilities

- Manage digital patient documents (upload, storage, metadata, retrieval)

- Provide RESTful APIs for the **Smart Doc UI (SD-UI)** micro frontend

- Integrate with the existing **OH-API/BE**, which acts as a proxy and gateway for authentication and permission enforcement

- Follow Open Hospital's domain and security standards

## Technology Stack

- Java 21+

- Spring Boot 3.x

- Spring Web MVC

- Maven (build & dependency management)

- OpenAPI 3 (API specification & documentation)

## Architecture Context

![architecture-context.png](doc/architecture-context.png)

1. SD-BE is **not directly exposed** to the frontend. All communication from SD-UI is routed through the main **OH-API/BE**, which:

2. Validates authentication (JWT/session)

3. Checks user permissions

4. Proxies authorized requests to SD-BE

5. Returns the response to SD-UI

This ensures consistency with Open Hospital's existing security and access control patterns.
