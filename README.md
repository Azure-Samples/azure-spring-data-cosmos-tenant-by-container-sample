# Azure Cosmos DB Spring Data - Tenant by Container

This repo provides a basic sample for a multi-tenanted application where each tenant has it's own Azure Cosmos DB `container`.

## Features

- The application is a simple CRUD REST web service which creates `User` entries in each tenant. 
- At application startup, a custom `CosmosTemplate` is created for all existing containers in the database defined at `cosmos.tenantsDatabase` in `application.properties` and stored in a hashmap. 
- A default database and container is also created, and this makes use of `CosmosRepository` and `azure-spring-data-cosmos` for Java SQL API, but nothing is stored in these resources by the application.
- The application uses `WebRequestInterceptor` to capture a http request header of `TenantId`. This is used to identify the corresponding `User` container (tenant) from the hashmap. A new template and container will be dynamically created if none exists for `TenantId` in the hashmap.

## Some multi-tenancy considerations

Review our article on [Multitenancy and Azure Cosmos DB](https://learn.microsoft.com/azure/architecture/guide/multitenant/service/cosmos-db) for more guidance. 

This sample application fetches the value of the tenant from request header (TenantId). In a real-world application, it is up to you how to identify this while keeping your application secure. For example, you may want to fetch the identifier from a cookie, or other header name.

The approach of assigning a container (or database) to each tenant may be useful if it is absolutely necessary to strictly isolate performance for each tenant. However, you should carefully consider the trade-offs involved in taking this approach. 

Unlike traditional databases, Azure Cosmos DB provides the capability for transparent [partitioning and horizontal scaling](https://learn.microsoft.com/azure/cosmos-db/partitioning-overview), and therefore in many cases it is feasible to use the `partitionKey` as the tenant identifier. In general, this allows you to achieve the highest density of tenants and therefore the lowest price per tenant. Although it is not possible to strictly isolate performance or have unlimited storage in a given partition, there are various features and approaches, such as using [hierarchical partition keys](https://learn.microsoft.com/azure/cosmos-db/hierarchical-partition-keys) or [throughput reallocation](https://learn.microsoft.com/azure/cosmos-db/sql/distribute-throughput-across-partitions) which can mitigate these challenges. Meanwhile, some challenges you may face when giving each tenant it's own container or database are as follows:

- **Cost** - each container has a minimum [Request Unit](https://learn.microsoft.com/azure/cosmos-db/request-units) allocation. If your distribution of activity between tenants is highly asymmetrical, having a container or database for each tenant may not prove cost effective.
- **Client-side resources** - a single container can have millions of partitions, and all be served from a singleton Cosmos client. However, each container requires at least one CosmosClient. This may put significant memory/resource demands on your application code. For many thousands of tenants this may force you into a more complex application code setup in order to serve performance needs adequately.  

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
