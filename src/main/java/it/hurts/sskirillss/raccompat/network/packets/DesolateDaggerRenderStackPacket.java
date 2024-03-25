package it.hurts.sskirillss.raccompat.network.packets;

import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DesolateDaggerRenderStackPacket {
    private final ItemStack stack;
    private final int id;

    public DesolateDaggerRenderStackPacket(FriendlyByteBuf buf) {
        stack = buf.readItem();
        id = buf.readInt();
    }

    public DesolateDaggerRenderStackPacket(ItemStack stack, int id) {
        this.stack = stack;
        this.id = id;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(stack);
        buf.writeInt(id);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!(Minecraft.getInstance().level.getEntity(id) instanceof DesolateDaggerEntity entity))
                return;

            entity.daggerRenderStack = stack;
        });

        return true;
    }
}