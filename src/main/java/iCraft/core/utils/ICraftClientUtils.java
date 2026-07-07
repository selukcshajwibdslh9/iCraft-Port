package iCraft.core.utils;

import iCraft.core.item.ItemiCraft;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class ICraftClientUtils {
    public static boolean hour24 = true;
    public static String homePage = "mod://mcef/home.html";
    private static Minecraft mc = FMLClientHandler.instance().getClient();

    public static EntityLivingBase getClientPlayer(WorldClient clientWorld, boolean isCalling) {
        List<NonNullList<ItemStack>> itemStacks = Arrays.asList(mc.player.inventory.mainInventory);
        Iterator i = itemStacks.iterator();

        ItemStack itemStack;
        do {
            if (!i.hasNext()) {
                return null;
            }

            itemStack = (ItemStack) i.next();
        } while (itemStack == null || !(itemStack.getItem() instanceof ItemiCraft) || itemStack.getTagCompound() == null);

        return isCalling ? clientWorld.getPlayerEntityByName(itemStack.getTagCompound().getString("calledPlayer")) : clientWorld.getPlayerEntityByName(itemStack.getTagCompound().getString("callingPlayer"));
    }

    public static int getPlayerNumber(boolean isCalling) {
        List<NonNullList<ItemStack>> itemStacks = Arrays.asList(mc.player.inventory.mainInventory);
        Iterator i = itemStacks.iterator();

        ItemStack itemStack;
        do {
            if (!i.hasNext()) {
                return 0;
            }

            itemStack = (ItemStack) i.next();
        } while (itemStack == null || !(itemStack.getItem() instanceof ItemiCraft) || itemStack.getTagCompound() == null);

        return isCalling ? itemStack.getTagCompound().getInteger("calledNumber") : itemStack.getTagCompound().getInteger("callingNumber");
    }

    public static ResourceLocation getResource(ICraftClientUtils.ResourceType type, String name) {
        return new ResourceLocation("icraft", type.getPrefix() + name);
    }

    public static String getTime() {
        long time = (mc.world.getWorldTime() + 6000L) % (hour24 ? 24000L : 12000L);
        long hours = time / 1000L;
        long seconds = (long) ((double) (time % 1000L) * 0.06D);
        return String.format("%02d", hours) + ":" + String.format("%02d", seconds);
    }

    public static String getAuthor(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            javazoom.jl.decoder.Bitstream bitstream = new javazoom.jl.decoder.Bitstream(bis);
            // JLayer не читает ID3v2, но читает ID3v1 через header
            // Используем стандартный способ — читаем ID3v1 tag вручную (последние 128 байт файла)
            return readID3v1Author(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readID3v1Author(File file) {
        // ID3v1 tag — последние 128 байт файла
        // Структура: TAG(3) + title(30) + artist(30) + album(30) + year(4) + comment(30) + genre(1)
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() < 128) return null;

            raf.seek(raf.length() - 128);
            byte[] tag = new byte[128];
            raf.readFully(tag);

            // Проверяем маркер "TAG"
            if (tag[0] != 'T' || tag[1] != 'A' || tag[2] != 'G') return null;

            // Artist: байты 33–62 (30 байт)
            String artist = new String(tag, 33, 30, java.nio.charset.StandardCharsets.ISO_8859_1).trim();
            return artist.isEmpty() ? null : artist;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static enum ResourceType {
        GUI("gui"),
        SOUND("sounds"),
        TEXTURE_BLOCKS("textures/blocks"),
        TEXTURE_ITEMS("textures/items"),
        RENDER("render");

        private String prefix;

        private ResourceType(String s) {
            this.prefix = s;
        }

        public String getPrefix() {
            return this.prefix + "/";
        }
    }
}
