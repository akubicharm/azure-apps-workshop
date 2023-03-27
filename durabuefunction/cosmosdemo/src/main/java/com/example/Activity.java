package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import java.util.*;

import org.json.*;

import com.microsoft.durabletask.*;
import com.microsoft.durabletask.azurefunctions.DurableActivityTrigger;
import com.microsoft.durabletask.azurefunctions.DurableClientContext;
import com.microsoft.durabletask.azurefunctions.DurableClientInput;
import com.microsoft.durabletask.azurefunctions.DurableOrchestrationTrigger;

public class Activity {

    /**
     * This is the activity function that gets invoked by the orchestration.
     */
    @FunctionName("Capitalize")
    public String capitalize(
            @DurableActivityTrigger(name = "name") String name,            
            final ExecutionContext context,
            @CosmosDBOutput(name = "database",
              databaseName = "mydb",
              containerName = "myresult",
              connection = "CosmosDBConnection")
            OutputBinding<String> outputItem) {
        context.getLogger().info("Capitalizing: " + name);
        // JSONArray array = new JSONArray(name);
        // outputItem.setValue(array.getJSONObject(0).toString());        
        return name.toUpperCase();
    }
}