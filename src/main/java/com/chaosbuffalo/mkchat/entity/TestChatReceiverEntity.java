package com.chaosbuffalo.mkchat.entity;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.DialoguePrompt;
import com.chaosbuffalo.mkchat.dialogue.DialogueTree;
import com.chaosbuffalo.mkchat.dialogue.PromptString;
import com.chaosbuffalo.mkchat.init.ChatEntityTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class TestChatReceiverEntity extends PigEntity implements IPlayerChatReceiver{
    private final DialogueComponent dialogueComponent;

    public TestChatReceiverEntity(final EntityType<? extends TestChatReceiverEntity> entityType, World world) {
        super(entityType, world);
        this.dialogueComponent = createDialogueComponent();
        setCustomName(new StringTextComponent("Talking Pig"));
    }

    protected DialogueComponent createDialogueComponent(){
        DialogueTree dialogueTree = new DialogueTree("testReceiver");
        ITextComponent msg = new StringTextComponent("Hello, do you ");
        DialoguePrompt testPrompt = dialogueTree.addPrompt("need xp", "I need xp.");
        PromptString promptString = new PromptString(testPrompt, "need some xp?");
        msg.appendSibling(promptString.getTextComponent());
        DialogueNode rootNode = dialogueTree.addNode(msg);
        dialogueTree.setStartNode(rootNode);
        DialogueNode responseNode = dialogueTree.addNode(new StringTextComponent("Here is 1 level."));
        responseNode.setCallback((player) -> player.addExperienceLevel(1));
        testPrompt.setResultNode(responseNode);
        return new DialogueComponent(this, dialogueTree);
    }

    public TestChatReceiverEntity(World world){
        this(ChatEntityTypes.TEST_CHAT.get(), world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(1, new LookRandomlyGoal(this));
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        if (!player.world.isRemote()){
            dialogueComponent.startDialogue((ServerPlayerEntity) player);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void receiveMessage(ServerPlayerEntity player, String message) {
        MKChat.LOGGER.info("Received chat message from {}, {}", player, message);
        dialogueComponent.receiveMessageFromPlayer(player, message);
    }
}
