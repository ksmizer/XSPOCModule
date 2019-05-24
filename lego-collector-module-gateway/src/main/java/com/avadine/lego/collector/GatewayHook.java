package com.avadine.lego.collector;

import java.util.concurrent.TimeUnit;

import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private TagManager tagManager;
    private ExecutionManager executionManager;
    
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
        executionManager = context.createExecutionManager("Lego Collector", 8);
        // Uncomment line below after runnable is created and replace {runnableFunction} with function name
        // executionManager.registerAtFixedRate("Lego", "Collector", {runnableFunction}, 600, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        // Also this line
        // executionManager.unRegister("Lego", "Collector");
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

    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        manager.addScriptModule(
                "system.lego.triggerCollector",
                ScriptFunctions.class,
                new PropertiesFileDocProvider());
    }
    
    public TagManager getTagManager() {
        return tagManager;
    }
}
