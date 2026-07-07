package iCraft.core.network;

import iCraft.core.entity.EntityPackingCase;
import iCraft.core.utils.ICraftUtils;
import iCraft.core.utils.TradeOffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageCraftBay extends MessageBase<MessageCraftBay> {
    private Item buyItem;
    private int buyQnt;
    private int buyMeta;
    private Item sellItem;
    private int sellQnt;
    public int amount;
    private int sellMeta;
    private ItemStack buyStack;
    private ItemStack sellStack;

    public MessageCraftBay() {
    }

    public MessageCraftBay(Item buyItem, int buyMeta, int buyCount, Item sellItem, int sellMeta, int sellCount, int amount) {
        this.buyItem = buyItem;
        this.buyQnt = buyCount;
        this.buyMeta = buyMeta;
        this.sellItem = sellItem;
        this.sellQnt = sellCount;
        this.sellMeta = sellMeta;
        this.amount = amount;
    }

    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, new ItemStack(this.buyItem, 1, this.buyMeta));
        buf.writeInt(this.buyQnt);
        ByteBufUtils.writeItemStack(buf, new ItemStack(this.sellItem, 1, this.sellMeta));
        buf.writeInt(this.sellQnt);
        buf.writeInt(this.amount);
    }

    public void fromBytes(ByteBuf buf) {
        this.buyStack = ByteBufUtils.readItemStack(buf);
        this.buyQnt = buf.readInt();
        this.sellStack = ByteBufUtils.readItemStack(buf);
        this.sellQnt = buf.readInt();
        this.amount = buf.readInt();
    }

    public void handleClientSide(MessageCraftBay message, EntityPlayer player) {
    }

    public void handleServerSide(MessageCraftBay message, EntityPlayer player) {
        if (player.getServer() != null) {
            final World world = player.getEntityWorld();
            final ItemStack finalBuyStack = message.buyStack.copy();
            finalBuyStack.setCount(message.amount);
            final ItemStack finalSellStack = message.sellStack.copy();
            finalSellStack.setCount(message.amount);
            final String playerName = player.getGameProfile().getName();
            final java.util.UUID playerId = player.getGameProfile().getId();
            final double posX = player.posX;
            final double posY = player.posY;
            final double posZ = player.posZ;

            player.getServer().addScheduledTask(() -> {
                int foundIndex = -1;
                for (int i = 0; i < ICraftUtils.tradeOffers.size(); i++) {
                    TradeOffer offer = ICraftUtils.tradeOffers.get(i);
                    if (offer.sellStack.isItemEqual(finalSellStack)) {
                        foundIndex = i;
                        break;
                    }
                }

                if (foundIndex != -1) {
    // 1. finalSellStack — это то, что ЛЕЖИТ В КОРОБКЕ (товар)
    // 2. finalBuyStack — это то, что ты ОТДАЛ (цена)
    EntityPackingCase packingCase = new EntityPackingCase(
        world, 
        new ItemStack[]{finalSellStack}, // То, что внутри коробки
        finalBuyStack,                   // То, что является ценой
        playerId, 
        playerName, 
        foundIndex
    );
    
    packingCase.setPosition(posX, posY + 75.0D, posZ);
    world.spawnEntity(packingCase);
}
            });
        }
    }
}