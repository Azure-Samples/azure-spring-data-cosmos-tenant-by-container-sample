# Azure Cosmos DB Spring Data - Tenant by Container

Spring Data sample for a multi-tenanted app where each tenant has its own Azure Cosmos DB `container`.

## Features

- The application is a simple CRUD REST web service which creates `User` entries in each tenant, and makes use of  `azure-spring-data-cosmos` for Azure Cosmos DB SQL API.
- At application startup, all the names of existing containers in the `tenants` database are retrieved and stored in `tenantList` in `TenantStorage` class. This class also contains resources to create Cosmos containers named by `tenantId` dynamically. The `tenants` database is created if it does not exists.
- The application uses `WebRequestInterceptor` to capture a http request header of `TenantId`. This is used to check if the corresponding `User` container (tenant id) exists in `tenantList`. If it does not, the container will be created.
- CRUD operations are performed in UserController using `cosmosTemplate` which is auto-wired along with `tenantStorage` which is used to create and reference Cosmos containers dynamically.

## Multi-tenancy considerations

This sample application fetches the value of the tenant from request header (TenantId). In a real-world application, it is up to you how to identify this while keeping your application secure. For example, you may want to fetch the identifier from a cookie, or other header name. The approach of assigning a container (or database) to each tenant may be useful if it is necessary to strictly isolate performance for each tenant. However, you should consider the trade-offs in taking this approach. Review our article on [Multitenancy and Azure Cosmos DB](https://learn.microsoft.com/azure/architecture/guide/multitenant/service/cosmos-db) for more guidance.
 

## Getting Started

### Prerequisites

- `Java Development Kit 8`.
- An active Azure account. If you don't have one, you can sign up for a [free account](https://azure.microsoft.com/free/). Alternatively, you can use the [Azure Cosmos DB Emulator](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator) for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store, [explained here](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates).
- [Apache Maven](https://maven.apache.org/install.html).
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](http://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.


SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](http://www.slf4j.org/manual.html) for more information.

### Quickstart

1. The app uses environment variables `ACCOUNT_HOST` and `ACCOUNT_KEY`. Make sure these environment variables exist, and are set to your Azure Cosmos DB account `URI` and `PRIMARY KEY` respectively.
1. git clone https://github.com/Azure-Samples/azure-spring-data-cosmos-tenant-by-container-sample.git
1. cd azure-spring-data-cosmos-tenant-by-container-sample
1. start the application: `mvn spring-boot:run`
1. Send a request to the web service from a linux based command line (or you can use [postman](https://www.postman.com/downloads/)): `curl -s -d '{"firstName":"Theo","lastName":"van Kraay"}' -H "Content-Type: application/json" -H "TenantId: theo" -X POST http://localhost:8080/users`


## Resources

Please refer to azure spring data cosmos for sql api [source code](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos) for more information.
