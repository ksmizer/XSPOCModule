package com.avadine.lego.collector.client;

import com.avadine.lego.collector.AbstractScriptModule;
import com.avadine.lego.collector.CollectorBlackBox;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;

public class ClientScriptModule extends AbstractScriptModule {

    private final CollectorBlackBox rpc;

    public ClientScriptModule() {
        rpc = ModuleRPCFactory.create(
            "com.inductiveautomation.ignition.examples.scripting-function",
            CollectorBlackBox.class
        );
    }

    @Override
    protected int[] triggerCollectorImpl() {
        return rpc.triggerCollector();
    }

}
