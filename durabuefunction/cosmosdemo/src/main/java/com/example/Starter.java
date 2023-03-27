package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import java.time.Duration;
import java.util.*;

import com.microsoft.durabletask.*;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

import org.json.*;



/**
 * Please follow the below steps to run this durable function sample
 * 1. Send an HTTP GET/POST request to endpoint `StartHelloCities` to run a durable function
 * 2. Send request to statusQueryGetUri in `StartHelloCities` response to get the status of durable function
 * For more instructions, please refer https://aka.ms/durable-function-java
 * 
 * Please add com.microsoft:durabletask-azure-functions to your project dependencies
 * Please add `"extensions": { "durableTask": { "hubName": "JavaTestHub" }}` to your host.json
 */
public class Starter {
    /**
     * This HTTP-triggered function starts the orchestration.
     */
    @FunctionName("StartOrchestration")
    public HttpResponseMessage startOrchestration(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        DurableTaskClient client = durableContext.getClient();
        String instanceId = client.scheduleNewOrchestrationInstance("Cities");
        context.getLogger().info("Created new Java orchestration with instance ID = " + instanceId);
        // return durableContext.createCheckStatusResponse(request, instanceId);


        context.getLogger().info("########## Wait for Instance Completion");
        OrchestrationMetadata metadata = null;
        try {
            metadata = client.getInstanceMetadata(instanceId, false);
            context.getLogger().info(metadata.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // context.getLogger().info("##########" + metadata.toString());
        return durableContext.createCheckStatusResponse(request, instanceId);

     }

    /**
     * CosmosDBTriggerの方をString[]にするとErrorになる。。。
     */
    @FunctionName("CosmosStarter")
    public void startCosmosOrchesturation(
            @CosmosDBTrigger(name = "item", databaseName = "mydb", containerName = "mycollection", connection = "CosmosDBConnection", leaseContainerName = "leases", createLeaseContainerIfNotExists = true) String input,
            @DurableClientInput(name = "durableContext") DurableClientContext durableContext,
            final ExecutionContext context) {
        context.getLogger().info("CosmosDB Trigger  ######" + input);

        DurableTaskClient client = durableContext.getClient();
        String instanceId = client.scheduleNewOrchestrationInstance("CosmosCities", input);
        context.getLogger().info("Created new Java orchestration with instance ID = " + instanceId);

        OrchestrationMetadata metadata = null;
        try {
            context.getLogger().info("Wait for completaion");
            context.getLogger().info(client.getInstanceMetadata(instanceId, false).toString());
            //metadata = client.waitForInstanceCompletion(instanceId, Duration.ofSeconds(5), true);

            metadata = client.getInstanceMetadata(instanceId, true);
            while (!metadata.isCompleted()) {
                Thread.sleep(1000);
                metadata = client.getInstanceMetadata(instanceId, true);
            }

            context.getLogger().info("metadata:" + metadata.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        context.getLogger().info("Method finish");
    }

    @FunctionName("CosmosCities")
    public String cosmosCitiesOrchestrator(
            @DurableOrchestrationTrigger(name = "taskOrchestrationContext") TaskOrchestrationContext ctx) {
        String result = "";
        // result += ctx.callActivity("Capitalize", "Tokyo", String.class).await() + ", ";
        // result += ctx.callActivity("Capitalize", "London", String.class).await() + ", ";
        // result += ctx.callActivity("Capitalize", "Seattle", String.class).await() + ", ";
        // result += ctx.callActivity("Capitalize", "Austin", String.class).await();

        String input = ctx.getInput(String.class);
        result += ctx.callActivity("Capitalize", input, String.class).await();

        return result;
    } 

}
