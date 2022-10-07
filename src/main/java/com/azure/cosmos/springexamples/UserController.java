package com.azure.cosmos.springexamples;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.springexamples.tenant.TenantStorage;
import com.azure.cosmos.springexamples.tenant.TenantService;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantService tenants;

    @Autowired
    private Environment env;

    private CosmosTemplate dynamicUserRepository;

    @PostMapping
    public @ResponseBody String createUser(@RequestBody User user) {
        String tenantId = TenantStorage.getCurrentTenant();
        //lookup dynamicUserRepository from hashmap of cosmos templates created at application start up
        //if no template exists for this tenant id, it is created dynamically here and added to the hashmap...
        dynamicUserRepository = tenants.getTenantTemplate(tenantId);
        UUID uuid = UUID.randomUUID();
        user.setId(String.valueOf(uuid));
        dynamicUserRepository.insert(tenantId, user);
        return String.format("Added %s.", user);
    }

    @GetMapping
    public @ResponseBody String getAllUsers() {
        String tenantId = TenantStorage.getCurrentTenant();
        //lookup dynamicUserRepository from hashmap of cosmos templates created at application start up
        //if no template exists for this tenant id, it is created dynamically here and added to the hashmap...
        dynamicUserRepository = tenants.getTenantTemplate(tenantId);
        Iterable<User> iter =  dynamicUserRepository.findAll(tenantId, User.class);
        return StreamSupport.stream(iter.spliterator(), true)
                .map(User::toString)
                .collect(Collectors.joining(" , "));
    }

    @GetMapping("/{id}")
    public @ResponseBody String getUser(@PathVariable UUID id) {
        String tenantId = TenantStorage.getCurrentTenant();
        //lookup dynamicUserRepository from hashmap of cosmos templates created at application start up
        //if no template exists for this tenant id, it is created dynamically here and added to the hashmap...
        dynamicUserRepository = tenants.getTenantTemplate(tenantId);
        User user = dynamicUserRepository.findById(tenantId, id, User.class);
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
        //lookup dynamicUserRepository from hashmap of cosmos templates created at application start up
        //if no template exists for this tenant id, it is created dynamically here and added to the hashmap...
        dynamicUserRepository = tenants.getTenantTemplate(tenantId);
        User user = dynamicUserRepository.findById(tenantId, id, User.class);
        if(user != null){
            dynamicUserRepository.deleteById(tenantId,id, new PartitionKey(user.getLastName()));
            return "Deleted " + id;
        }
        else{
            return "no users found for this tenant!";
        }
    }
}
