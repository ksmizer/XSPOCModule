package pom;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.sqltags.model.TagManager;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private GatewayContext context;
    private TagManager tagManager;
    
    @Override
    public void setup(GatewayContext gatewayContext) {
        context = gatewayContext;
        tagManager = gatewayContext.getTagManager();
    }

    @Override
    public void startup(LicenseState licenseState) {
        
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean isFreeModule() {
        return true;
    }
}
