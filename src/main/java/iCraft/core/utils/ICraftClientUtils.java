package iCraft.core.utils;

import iCraft.core.item.ItemiCraft;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ICraftClientUtils {
    public static boolean hour24 = true;
    public static String homePage = "mod://mcef/home.html";

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static EntityLivingBase getClientPlayer(WorldClient clientWorld, boolean isCalling) {
        if (clientWorld == null || mc.player == null) {
            return null;
        }

        for (ItemStack itemStack : mc.player.inventory.mainInventory) {

            if (itemStack.isEmpty()) {
                continue;
            }

            if (!(itemStack.getItem() instanceof ItemiCraft)) {
                continue;
            }

            if (!itemStack.hasTagCompound()) {
                continue;
            }

            String playerName = isCalling
                    ? itemStack.getTagCompound().getString("calledPlayer")
                    : itemStack.getTagCompound().getString("callingPlayer");

            return clientWorld.getPlayerEntityByName(playerName);
        }

        return null;
    }

    public static int getPlayerNumber(boolean isCalling) {
        if (mc.player == null) {
            return 0;
        }

        for (ItemStack itemStack : mc.player.inventory.mainInventory) {

            if (itemStack.isEmpty()) {
                continue;
            }

            if (!(itemStack.getItem() instanceof ItemiCraft)) {
                continue;
            }

            if (!itemStack.hasTagCompound()) {
                continue;
            }

            return isCalling
                    ? itemStack.getTagCompound().getInteger("calledNumber")
                    : itemStack.getTagCompound().getInteger("callingNumber");
        }

        return 0;
    }

    public static ResourceLocation getResource(ResourceType type, String name) {
        return new ResourceLocation("icraft", type.getPrefix() + name);
    }

    public static String getTime() {
        if (mc.world == null) {
            return "--:--";
        }

        long time = (mc.world.getWorldTime() + 6000L)
                % (hour24 ? 24000L : 12000L);

        long hours = time / 1000L;
        long seconds = (long) ((time % 1000L) * 0.06D);

        return String.format("%02d:%02d", hours, seconds);
    }

    public static String getAuthor(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            return readID3v1Author(file);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String readID3v1Author(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() < 128) {
                return null;
            }

            raf.seek(raf.length() - 128);

            byte[] tag = new byte[128];
            raf.readFully(tag);

            if (tag[0] != 'T' || tag[1] != 'A' || tag[2] != 'G') {
                return null;
            }

            String artist = new String(tag, 33, 30, java.nio.charset.StandardCharsets.ISO_8859_1).trim();

            return artist.isEmpty() ? null : artist;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public enum ResourceType {
        GUI("gui"),
        SOUND("sounds"),
        TEXTURE_BLOCKS("textures/blocks"),
        TEXTURE_ITEMS("textures/items"),
        RENDER("render");

        private final String prefix;

        ResourceType(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix + "/";
        }
    }
}
