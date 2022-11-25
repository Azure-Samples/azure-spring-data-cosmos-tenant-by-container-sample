package com.azure.cosmos.springexamples.tenant;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.util.CosmosPagedFlux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@PropertySource("classpath:application.properties")
public class TenantStorage implements CommandLineRunner {
    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();
    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }
    public static String getCurrentTenant() {
        return currentTenant.get();
    }
    public static void clear() {
        currentTenant.remove();
    }
    private static final Logger logger = LoggerFactory.getLogger(TenantStorage.class);
    private Environment env;
    CosmosAsyncClient client;
    CosmosAsyncDatabase database;
    ConcurrentLinkedQueue<String> tenantList = new ConcurrentLinkedQueue<String>();
    CosmosPagedFlux<CosmosContainerProperties> containers;
    public TenantStorage(Environment env){
        this.env = env;
    }
    public CosmosAsyncClient getClient(){
        return this.client;
    };
    public String getTenant(String tenantId){
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(tenantId, "/lastName");
        //create container if it does not exist
        Boolean tenant = tenantList.contains(tenantId);
        if(!tenant){
            client.getDatabase(getTenantsDatabase().getId()).createContainerIfNotExists(containerProperties).block();
            tenantList.add(tenantId);
        }
        return tenantId;
    }
    public CosmosAsyncDatabase getTenantsDatabase(){
        if(this.database!=null){
            return this.database;
        }
        else{
            this.client = new CosmosClientBuilder().endpoint(env.getProperty("cosmos.uri")).key(env.getProperty("cosmos.key")).contentResponseOnWriteEnabled(true).buildAsyncClient();
            CosmosDatabaseResponse databaseResponse = this.client.createDatabaseIfNotExists(env.getProperty("cosmos.databaseName")).block();
            return this.database = this.client.getDatabase(databaseResponse.getProperties().getId());
        }
    }

    @Override
    public void run(String...args) throws Exception {
        CosmosAsyncDatabase database = getTenantsDatabase();
        containers = database.readAllContainers();
        String msg="Listing containers in tenants database:\n";
        containers.byPage(100).flatMap(readAllContainersResponse -> {
            logger.info("read {} containers(s) with request charge of {}", readAllContainersResponse.getResults().size(),readAllContainersResponse.getRequestCharge());
            for (CosmosContainerProperties response : readAllContainersResponse.getResults()) {
                String tenantId = response.getId();
                logger.info("container tenant id: {}", tenantId);
                logger.info("adding {} to tenant list", tenantId);
                tenantList.add(tenantId);
            }
            return Flux.empty();
        }).blockLast();
    }
}