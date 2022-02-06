package com.chaosbuffalo.mkchat.capabilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.capabilities.Capability;

public class PlayerDialogueProvider extends ChatCapabilities.Provider<PlayerEntity, IPlayerDialogue> {

    public PlayerDialogueProvider(PlayerEntity player) {
        super(player);
    }

    @Override
    PlayerDialogueHandler makeData(PlayerEntity attached) {
        return new PlayerDialogueHandler(attached);
    }

    @Override
    Capability<IPlayerDialogue> getCapability() {
        return ChatCapabilities.PLAYER_DIALOGUE_CAPABILITY;
    }
}
