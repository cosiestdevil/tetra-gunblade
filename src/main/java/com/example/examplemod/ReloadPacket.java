package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import se.mickelus.mutil.network.AbstractPacket;

public class ReloadPacket extends AbstractPacket {
    private InteractionHand hand;

    public ReloadPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public ReloadPacket() {
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        this.hand = buffer.readEnum(InteractionHand.class);
    }

    @Override
    public void handle(Player player) {
        var stack = player.getItemInHand(this.hand);
        if (stack.is(ExampleMod.EXAMPLE_ITEM.get())){
            ((ModularGunBlade)stack.getItem()).reloadAmmo(player,stack);
        }
    }
}
