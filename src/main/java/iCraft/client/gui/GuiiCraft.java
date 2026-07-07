package iCraft.client.gui;

import iCraft.client.InternetHandler;
import iCraft.core.ICraft;
import iCraft.core.utils.ICraftUtils;
import java.io.IOException;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiiCraft extends GuiiCraftBase {
    public GuiiCraft(String resource) {
        super(resource);
    }

    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
        int xAxis = mouseX - super.guiWidth;
        int yAxis = mouseY - super.guiHeight;
        if (xAxis >= 51 && xAxis <= 67 && yAxis >= 38 && yAxis <= 54) {
            this.drawTexturedModalRect(super.guiWidth + 51, super.guiHeight + 38, 176, 38, 17, 17);
        }

        if (xAxis >= 70 && xAxis <= 86 && yAxis >= 38 && yAxis <= 54) {
            this.drawTexturedModalRect(super.guiWidth + 70, super.guiHeight + 38, 176, 55, 17, 17);
        }
        if (xAxis >= 89 && xAxis <= 105 && yAxis >= 58 && yAxis <= 74) {
           this.drawTexturedModalRect(super.guiWidth + 89, super.guiHeight + 58, 176, 55, 17, 17);
        }
        if (xAxis >= 108 && xAxis <= 124 && yAxis >= 38 && yAxis <= 54) {
            this.drawTexturedModalRect(super.guiWidth + 108, super.guiHeight + 38, 176, 89, 17, 17);
        }

        if (xAxis >= 51 && xAxis <= 67 && yAxis >= 58 && yAxis <= 74) {
            this.drawTexturedModalRect(super.guiWidth + 51, super.guiHeight + 58, 193, 38, 17, 17);
        }

        if (xAxis >= 70 && xAxis <= 86 && yAxis >= 58 && yAxis <= 74) {
            this.drawTexturedModalRect(super.guiWidth + 70, super.guiHeight + 58, 193, 55, 17, 17);
        }

        if (xAxis >= 89 && xAxis <= 105 && yAxis >= 58 && yAxis <= 74) {
            this.drawTexturedModalRect(super.guiWidth + 89, super.guiHeight + 58, 193, 72, 17, 17);
        }

        if (xAxis >= 108 && xAxis <= 124 && yAxis >= 58 && yAxis <= 74) {
            this.drawTexturedModalRect(super.guiWidth + 108, super.guiHeight + 58, 193, 89, 17, 17);
        }

        if (xAxis >= 51 && xAxis <= 67 && yAxis >= 78 && yAxis <= 94) {
            this.drawTexturedModalRect(super.guiWidth + 51, super.guiHeight + 78, 210, 38, 17, 17);
        }

    }

    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.drawTime();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    protected void mouseClicked(int x, int y, int button) {
        try {
            super.mouseClicked(x, y, button);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (button == 0) {
            int xAxis = x - super.guiWidth;
            int yAxis = y - super.guiHeight;
            if (xAxis >= 51 && xAxis <= 67 && yAxis >= 38 && yAxis <= 54) {
                super.mc.player.openGui(ICraft.instance, 1, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 70 && xAxis <= 86 && yAxis >= 38 && yAxis <= 54) {
                if (Loader.isModLoaded("mcef") && !(super.mc.currentScreen instanceof GuiiCraftBrowser)) {
                    super.mc.displayGuiScreen(InternetHandler.hasBackup() ? InternetHandler.backup : new GuiiCraftBrowser());
                    InternetHandler.backup = null;
                } else {
                    super.mc.player.sendMessage(new TextComponentString("[iCraft] You need MCEF in order to use this function."));
                    TextComponentString linkText = new TextComponentString("https://github.com/V5Minecraft/mcef-patched/releases/download/MCEF-1.11b-patched/mcef-1.12.2-1.11b.jar");
                    Style style = new Style();
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/V5Minecraft/mcef-patched/releases/download/MCEF-1.11b-patched/mcef-1.12.2-1.11b.jar"));
                    style.setColor(TextFormatting.AQUA);
                    style.setUnderlined(true);
                    linkText.setStyle(style);
                    TextComponentString message = new TextComponentString("[iCraft] Link to download MCEF: ");
                    message.appendSibling(linkText);
                    super.mc.player.sendMessage(message);
                }
            }

            if (xAxis >= 89 && xAxis <= 105 && yAxis >= 38 && yAxis <= 54) {
                super.mc.player.openGui(ICraft.instance, 3, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 108 && xAxis <= 124 && yAxis >= 38 && yAxis <= 54) {
                super.mc.player.openGui(ICraft.instance, 4, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 51 && xAxis <= 67 && yAxis >= 58 && yAxis <= 74) {
                super.mc.player.openGui(ICraft.instance, 5, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 70 && xAxis <= 86 && yAxis >= 58 && yAxis <= 74) {
                super.mc.player.openGui(ICraft.instance, 8, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 89 && xAxis <= 105 && yAxis >= 58 && yAxis <= 74 && ICraft.isIBayActive && !ICraftUtils.tradeOffers.isEmpty()) {
                super.mc.player.openGui(ICraft.instance, 9, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 108 && xAxis <= 124 && yAxis >= 58 && yAxis <= 74) {
                super.mc.player.openGui(ICraft.instance, 10, super.mc.world, 0, 0, 0);
            }

            if (xAxis >= 51 && xAxis <= 67 && yAxis >= 78 && yAxis <= 94) {
                super.mc.player.openGui(ICraft.instance, 12, super.mc.world, 0, 0, 0);
            }
        }
    }
}
