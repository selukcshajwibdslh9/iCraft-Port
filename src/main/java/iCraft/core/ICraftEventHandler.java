package iCraft.core;

import iCraft.core.item.ItemiCraft;
import iCraft.core.network.MessageClosePlayer;
import iCraft.core.network.NetworkHandler;
import iCraft.core.utils.ICraftUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

public class ICraftEventHandler {
    public ICraftEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onItemExpire(ItemExpireEvent event) {
        if (event.getEntityItem() != null && event.getEntityItem().getItem().getItem() instanceof ItemiCraft) {
            ItemStack iCraft = event.getEntityItem().getItem();
            if (iCraft.getTagCompound() != null && iCraft.getTagCompound().hasKey("called") && iCraft.getTagCompound().getInteger("called") != 0) {
                ICraftUtils.changeCalledStatus(iCraft, 0, 0, iCraft.getTagCompound().getBoolean("isCalling"));
            }
        }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && this.willEntityDie(event)) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            for (ItemStack itemStack : player.inventory.mainInventory) {
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemiCraft) {
                    if (itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("called") && itemStack.getTagCompound().getInteger("called") != 0) {
                        ICraftUtils.changeCalledStatus(itemStack, 0, 0, itemStack.getTagCompound().getBoolean("isCalling"));
                    }
                    if (player instanceof EntityPlayerMP) {
                        NetworkHandler.sendTo(new MessageClosePlayer(), (EntityPlayerMP) player);
                    }
                }
            }
        }
    }

    public boolean willEntityDie(LivingHurtEvent event) {
        float amount = event.getAmount();
        DamageSource source = event.getSource();
        EntityLivingBase living = event.getEntityLiving();
        int resistance;
        if (!source.isUnblockable()) {
            resistance = 25 - living.getTotalArmorValue();
            amount = amount * (float) resistance / 25.0F;
        }
        if (living.isPotionActive(Objects.requireNonNull(Potion.getPotionById(11)))) {
            resistance = 25 - (Objects.requireNonNull(living.getActivePotionEffect(Objects.requireNonNull(Potion.getPotionById(11)))).getAmplifier() + 1) * 5;
            amount = amount * (float) resistance / 25.0F;
        }
        return Math.ceil(amount) >= Math.floor(living.getHealth());
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent event) {
        if (event.getModID().equals("iCraft")) {
            ICraft.proxy.loadConfiguration();
            ICraft.proxy.onConfigSync();
        }
    }
}
