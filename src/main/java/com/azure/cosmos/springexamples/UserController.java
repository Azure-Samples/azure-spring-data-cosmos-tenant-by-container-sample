package com.azure.cosmos.springexamples;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.springexamples.tenant.TenantStorage;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@PropertySource("classpath:application.properties")
@Controller
@RequestMapping(path = "/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final TenantStorage tenantStorage;
    private final CosmosTemplate cosmosTemplate;
    public UserController( TenantStorage tenantStorage, Environment env, CosmosTemplate cosmosTemplate) {
        this.tenantStorage = tenantStorage;
        this.cosmosTemplate = cosmosTemplate;
    }

    @PostMapping
    public @ResponseBody String createUser(@RequestBody User user) {
        String tenantId = TenantStorage.getCurrentTenant();
        //retrieve string container name from list (if it does not exist yet, create it).
        logger.info("creating user...");
        String tenantContainer = tenantStorage.getTenant(tenantId);
        UUID uuid = UUID.randomUUID();
        user.setId(String.valueOf(uuid));
        cosmosTemplate.insert(tenantContainer, user);
        logger.info("user created");
        return String.format("Added %s.", user);
    }

    @GetMapping
    public @ResponseBody String getAllUsers() {
        String tenantId = TenantStorage.getCurrentTenant();
        //retrieve string container name from list  (if it does not exist yet, create it).
        String tenantContainer = tenantStorage.getTenant(tenantId);
        Iterable<User> iter =  cosmosTemplate.findAll(tenantContainer, User.class);
        return StreamSupport.stream(iter.spliterator(), true)
                .map(User::toString)
                .collect(Collectors.joining(" , "));
    }

    @GetMapping("/{id}")
    public @ResponseBody String getUser(@PathVariable UUID id) {
        String tenantId = TenantStorage.getCurrentTenant();
        //retrieve string container name from list  (if it does not exist yet, create it).
        String tenantContainer = tenantStorage.getTenant(tenantId);
        User user = cosmosTemplate.findById(tenantContainer, id, User.class);
        String response = "no users found for this tenant!";
        if(user != null){
            logger.info("user: "+ user.getLastName());
            response = "first name: "+user.getFirstName() +", lastName: "+user.getLastName()+ ", id: "+user.getId();
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public @ResponseBody String deleteUser(@PathVariable UUID id) {
        String tenantId = TenantStorage.getCurrentTenant();
        //retrieve string container name from list  (if it does not exist yet, create it).
        String tenantContainer = tenantStorage.getTenant(tenantId);
        User user = cosmosTemplate.findById(tenantContainer, id, User.class);
        if(user != null){
            cosmosTemplate.deleteById(tenantId,id, new PartitionKey(user.getLastName()));
            return "Deleted " + id;
        }
        else{
            return "no users found for this tenant!";
        }
    }
}
