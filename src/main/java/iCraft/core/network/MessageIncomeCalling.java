package iCraft.core.network;

import iCraft.core.ICraft;
import iCraft.core.item.ItemiCraft;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class MessageIncomeCalling extends MessageBase<MessageIncomeCalling> {
   private int number;
   private int calledNumber;

   public MessageIncomeCalling() {
   }

   public MessageIncomeCalling(int number, int calledNumber) {
      this.number = number;
      this.calledNumber = calledNumber;
   }

   public void fromBytes(ByteBuf buf) {
      this.number = buf.readInt();
      this.calledNumber = buf.readInt();
   }

   public void toBytes(ByteBuf buf) {
      buf.writeInt(this.number);
      buf.writeInt(this.calledNumber);
   }

   public void handleClientSide(MessageIncomeCalling message, EntityPlayer player) {
      player.closeScreen();
      player.openGui(ICraft.instance, 6, player.world, 0, 0, 0);
   }

   @Override
   public void handleServerSide(MessageIncomeCalling message, EntityPlayer player) {
      World world = player.world;

      for (EntityPlayer targetPlayer : world.playerEntities) {

         if (targetPlayer == player) {
            continue;
         }

         for (ItemStack targetPhone : targetPlayer.inventory.mainInventory) {

            if (targetPhone.isEmpty()) {
               continue;
            }

            if (!(targetPhone.getItem() instanceof ItemiCraft)) {
               continue;
            }

            if (!targetPhone.hasTagCompound()) {
               continue;
            }

            NBTTagCompound targetTag = targetPhone.getTagCompound();

            if (!targetTag.hasKey("number")) {
               continue;
            }

            if (targetTag.getInteger("number") != message.calledNumber) {
               continue;
            }

            List<String> blackList = readNBT(targetTag);

            if (blackList.contains(player.getName())) {
               continue;
            }

            if (targetTag.hasKey("called")
                    && targetTag.getInteger("called") != 0) {
               continue;
            }

            targetTag.setInteger("called", 1);
            targetTag.setInteger("callingNumber", message.number);
            targetTag.setString("callingPlayer", player.getName());
            targetTag.setBoolean("isCalling", false);

            for (ItemStack callerPhone : player.inventory.mainInventory) {

               if (callerPhone.isEmpty()) {
                  continue;
               }

               if (!(callerPhone.getItem() instanceof ItemiCraft)) {
                  continue;
               }

               if (!callerPhone.hasTagCompound()) {
                  continue;
               }

               NBTTagCompound callerTag = callerPhone.getTagCompound();

               if (!callerTag.hasKey("number")) {
                  continue;
               }

               if (callerTag.getInteger("number") != message.number) {
                  continue;
               }

               callerTag.setInteger("called", 1);
               callerTag.setInteger(
                       "calledNumber",
                       targetTag.getInteger("number")
               );
               callerTag.setString(
                       "calledPlayer",
                       targetPlayer.getName()
               );
               callerTag.setBoolean("isCalling", true);

               NetworkHandler.sendTo(
                       this,
                       (EntityPlayerMP) player
               );

               return;
            }
         }
      }
   }

   private List<String> readNBT(NBTTagCompound nbtTags) {
      List<String> blackList = new ArrayList<>();

      NBTTagList tagList = nbtTags.getTagList("blacklist", 10);

      for (int i = 0; i < tagList.tagCount(); i++) {

         NBTTagCompound tagCompound =
                 tagList.getCompoundTagAt(i);

         String playerName =
                 tagCompound.getString("player" + i);

         if (!playerName.isEmpty()) {
            blackList.add(playerName);
         }
      }

      return blackList;
   }
}
