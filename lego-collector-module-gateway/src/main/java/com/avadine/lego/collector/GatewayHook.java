package com.avadine.lego.collector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleorm.dataset.SQuery;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private TagManager tagManager;
    private ExecutionManager executionManager;
    private Integer threads = 6;
    
    @Override
    public void setup(GatewayContext gatewayContext) {
        context = gatewayContext;
        tagManager = gatewayContext.getTagManager();

        try {
            gatewayContext.getSchemaUpdater().updatePersistentRecords(CollectorConfiguration.META);
        } catch (SQLException e) {
            logger.error("Error verifying schemas.", e);
        }

        BundleUtil.get().addBundle("Collector", GatewayHook.class, "Collector");
    }
    
    @Override
    public void startup(LicenseState licenseState) {

        List<Integer> ids = new ArrayList<Integer>();
        SQuery<CollectorConfiguration> query = new SQuery<CollectorConfiguration>(CollectorConfiguration.META);
        List<CollectorConfiguration> results = context.getPersistenceInterface().query(query);

        for (CollectorConfiguration result : results) {
            if (result.getEnabled()) {
                String username = result.getUsername();
                String password = result.getPassword();
                String connectionString = result.getConnectionString();
                CollectorDatabaseConnection dc = new CollectorDatabaseConnection(username, password, connectionString);
                ids.addAll(dc.getCollectionSourceIds());
                dc.closeConnection();
            }
        }

        executionManager = context.createExecutionManager("Lego Collector", threads);

        Integer rangeHigh = ids.size() / threads;

        for (Integer i = 0; i < threads; i++) {
            Integer increment = i*rangeHigh;
            List<Integer> collectionSourceIds = ids.subList(increment, increment + rangeHigh);
            executionManager.registerAtFixedRate("Lego", "Collector - Thread: " + Integer.toString(i+1), new CollectorRunnable(context, collectionSourceIds), 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void shutdown() {
        // Also this line
        executionManager.unRegister("Lego", "Collector");
        BundleUtil.get().removeBundle("Collector");
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }

    @Override
    public List<? extends IConfigTab> getConfigPanels() {
        return Lists.newArrayList(CollectorConfigurationPage.MENU_ENTRY);
    }

    @Override
    public List<ConfigCategory> getConfigCategories() {
        return Lists.newArrayList(CollectorConfigurationPage.CONFIG_CATEGORY);
    }

    // @Override
    // public void initializeScriptManager(ScriptManager manager) {
    //     super.initializeScriptManager(manager);

    //     manager.addScriptModule(
    //             "system.lego.triggerCollector",
    //             ScriptFunctions.class);
    // }
    
    public TagManager getTagManager() {
        return tagManager;
    }
}
