package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.ChatConstants;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueManager;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

public class DialogueComponent {
    private final LivingEntity entity;
    private final ResourceLocation treeName;

    public DialogueComponent(LivingEntity entity, ResourceLocation treeName){
        this.entity = entity;
        this.treeName = treeName;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public void startDialogue(ServerPlayerEntity player){
        DialogueTree tree = DialogueManager.getDialogueTree(treeName);
        if (tree != null && tree.getHailPrompt() != null) {
            if (player.getServer() != null){
                player.getServer().getPlayerList().sendToAllNearExcept(null,
                        player.getPosX(), player.getPosY(), player.getPosZ(), ChatConstants.CHAT_RADIUS,
                        player.dimension,
                        new SChatPacket(new StringTextComponent(String.format("<%s> Hail, %s",
                                player.getName().getFormattedText(), this.getEntity().getName().getFormattedText())),
                                ChatType.CHAT));
            }
            tree.getHailPrompt().handlePrompt(player, entity, tree);
        } else {
            MKChat.LOGGER.info("Failed to find dialogue {}", treeName);
        }
    }

    public void receiveMessageFromPlayer(ServerPlayerEntity playerEntity, String msg){
        DialogueTree tree = DialogueManager.getDialogueTree(treeName);
        if (tree != null){
            tree.handlePlayerMessage(playerEntity, msg, entity);
        }
    }
}
