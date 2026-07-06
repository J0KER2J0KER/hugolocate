package com.j0ker2j0ker.hugolocate.client;

import com.j0ker2j0ker.hugolocate.client.command.HugolocateCommand;
import net.fabricmc.api.ClientModInitializer;

public class HugolocateClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HugolocateCommand.register();
    }
}