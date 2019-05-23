package pom;

import java.util.concurrent.TimeUnit;

import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//Made Changes

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private TagManager tagManager;
    private ExecutionManager executionManager;
    
    @Override
    public void setup(GatewayContext gatewayContext) {
        context = gatewayContext;
        tagManager = gatewayContext.getTagManager();
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
    }

    @Override
    public boolean isFreeModule() {
        return true;
    }
}
