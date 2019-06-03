package com.avadine.xspoc.designer;

import com.avadine.xspoc.ScriptFunctions;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;

public class DesignerHook extends AbstractDesignerModuleHook {

    
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        manager.addScriptModule(
                "system.xspoc.triggerCollector",
                ScriptFunctions.class);
    }

}