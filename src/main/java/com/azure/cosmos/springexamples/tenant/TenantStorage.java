package com.azure.cosmos.springexamples.tenant;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired
    private Environment env;

    CosmosAsyncClient client;

    ConcurrentHashMap<String,CosmosTemplate> tenantTemplates =  new ConcurrentHashMap<String, CosmosTemplate>();

    CosmosPagedFlux<CosmosContainerProperties> containers;


    public CosmosPagedFlux<CosmosContainerProperties> getContainers(){
        return this.containers;
    }

    public CosmosTemplate getTenantTemplate(String tenantId){
        CosmosTemplate template = tenantTemplates.get(tenantId);
        if (template == null){
            logger.info("template does not exist yet for tenant: {}, creating...", tenantId);
            template = createTemplate(tenantId);
            logger.info("adding template for tenant: {} to hashmap for later re-use...", tenantId);
            tenantTemplates.put(tenantId,template);
        }
        else{
            logger.info("template exists for tenant: {}. Re-using...", tenantId);
        }
        return template;
    }

    public CosmosTemplate createTemplate (String tenantId){
        CosmosAsyncDatabase database = getTenantsDatabase();
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(tenantId, "/lastName");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
        database.createContainerIfNotExists(containerProperties, throughputProperties).block();
        CosmosConfig config = CosmosConfig.builder().build();
        final CosmosFactory cosmosFactory = new CosmosFactory(this.client, env.getProperty("cosmos.tenantsDatabase"));
        final CosmosMappingContext mappingContext = new CosmosMappingContext();
        final MappingCosmosConverter cosmosConverter = new MappingCosmosConverter(mappingContext, null);
        return new CosmosTemplate(this.client, env.getProperty("cosmos.tenantsDatabase"), config, cosmosConverter);
    }

    public CosmosAsyncDatabase getTenantsDatabase(){
        this.client = new CosmosClientBuilder().endpoint(env.getProperty("cosmos.uri")).key(env.getProperty("cosmos.key")).contentResponseOnWriteEnabled(true).buildAsyncClient();
        CosmosDatabaseResponse databaseResponse = this.client.createDatabaseIfNotExists(env.getProperty("cosmos.tenantsDatabase")).block();
        CosmosAsyncDatabase database = this.client.getDatabase(databaseResponse.getProperties().getId());
        return database;
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
                logger.info("creating cosmos template for existing tenant id: {}", tenantId);
                CosmosTemplate template = createTemplate(tenantId);
                logger.info("adding {} to tenant hashmap", tenantId);
                tenantTemplates.put(tenantId,template);
            }
            return Flux.empty();
        }).blockLast();
    }
}