package iCraft.client.gui;

import iCraft.core.ICraft;
import iCraft.core.network.MessageCraftBay;
import iCraft.core.network.NetworkHandler;
import iCraft.core.utils.ICraftClientUtils;
import iCraft.core.utils.ICraftUtils;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiiCraftShopping extends GuiiCraftBase {
    private int i = 0;
    private int qnt = 1;

    public GuiiCraftShopping(String resource) {
        super(resource);
    }

    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(false);
        super.buttonList.clear();
        super.buttonList.add(new GuiButton(0, super.guiWidth + 52, super.guiHeight + 75, 15, 20, "<"));
        super.buttonList.add(new GuiButton(1, super.guiWidth + 96, super.guiHeight + 75, 15, 20, ">"));
        super.buttonList.add(new GuiButton(2, super.guiWidth + 125, super.guiHeight + 49, 10, 10, "\u25B2"));
        super.buttonList.add(new GuiButton(3, super.guiWidth + 125, super.guiHeight + 58, 10, 10, "\u25BC"));
        super.buttonList.add(new GuiButton(4, super.guiWidth + 67, super.guiHeight + 75, 29, 20, "Buy"));
    }

public void updateScreen() {
    super.updateScreen();
    // Проверяем, что список не пуст, прежде чем брать элемент
    if (!ICraftUtils.tradeOffers.isEmpty() && this.i < ICraftUtils.tradeOffers.size()) {
        if (ICraftUtils.tradeOffers.get(this.i).buyStack.getCount() * this.qnt > 64 || 
            ICraftUtils.tradeOffers.get(this.i).sellStack.getCount() * this.qnt > 64) {
            this.qnt = 1;
        }
    }
}

public void drawScreen(int mouseX, int mouseY, float partialTick) {
    super.drawScreen(mouseX, mouseY, partialTick);
    
    // БЕЗОПАСНОСТЬ: Если список пуст, просто не рисуем предметы
    if (ICraftUtils.tradeOffers.isEmpty() || this.i >= ICraftUtils.tradeOffers.size()) return;

    GlStateManager.pushMatrix();
    RenderHelper.enableGUIStandardItemLighting();
        
        ItemStack buyStack = ICraftUtils.tradeOffers.get(this.i).buyStack;
        ItemStack sellStack = ICraftUtils.tradeOffers.get(this.i).sellStack;

        ItemStack buyItem = new ItemStack(buyStack.getItem(), buyStack.getCount() * this.qnt, buyStack.getItemDamage());
        ItemStack sellItem = new ItemStack(sellStack.getItem(), sellStack.getCount() * this.qnt, sellStack.getItemDamage());

// Меняем местами координаты 98 и 48
super.itemRender.renderItemAndEffectIntoGUI(buyItem, super.guiWidth + 48, super.guiHeight + 51); // Цена слева
super.itemRender.renderItemOverlays(super.fontRenderer, buyItem, super.guiWidth + 48, super.guiHeight + 51);

super.itemRender.renderItemAndEffectIntoGUI(sellItem, super.guiWidth + 98, super.guiHeight + 51); // Товар справа
super.itemRender.renderItemOverlays(super.fontRenderer, sellItem, super.guiWidth + 98, super.guiHeight + 51);

// И не забудь поменять координаты в подсказках (tooltip):
if (this.isMouseOver(48, 51, 16, 16, mouseX, mouseY)) { // Теперь цена слева
    this.renderToolTip(buyItem, mouseX, mouseY);
}
if (this.isMouseOver(98, 51, 16, 16, mouseX, mouseY)) { // Теперь товар справа
    this.renderToolTip(sellItem, mouseX, mouseY);
}

GlStateManager.popMatrix();
    }

protected void actionPerformed(GuiButton guiButton) {
    if (guiButton.enabled && !ICraftUtils.tradeOffers.isEmpty()) { // ПРОВЕРКА!
        int size = ICraftUtils.tradeOffers.size();
        // Убедись, что индекс 'i' всегда в рамках списка
        if (this.i >= size) this.i = 0; 
            switch (guiButton.id) {
                case 0: this.i = this.i - 1 >= 0 ? this.i - 1 : size - 1; break;
                case 1: this.i = this.i + 1 < size ? this.i + 1 : 0; break;
                case 2: 
                    if (ICraftUtils.tradeOffers.get(this.i).buyStack.getCount() * (this.qnt + 1) <= 64 && 
                        ICraftUtils.tradeOffers.get(this.i).sellStack.getCount() * (this.qnt + 1) <= 64) this.qnt++;
                    break;
                case 3: this.qnt = this.qnt - 1 > 0 ? this.qnt - 1 : this.qnt; break;
                case 4:
                if (ICraft.isIBayActive) {
                    ItemStack b = ICraftUtils.tradeOffers.get(this.i).buyStack;
                    ItemStack s = ICraftUtils.tradeOffers.get(this.i).sellStack;
        
        // Исправленный вызов с 7 параметрами
        NetworkHandler.sendToServer(new MessageCraftBay(
            b.getItem(), 
            b.getItemDamage(), 
            b.getCount(), // ДОБАВЛЕНА БАЗОВАЯ ЦЕНА
            s.getItem(), 
            s.getItemDamage(), 
            s.getCount(), // ДОБАВЛЕНО БАЗОВОЕ КОЛ-ВО НАГРАДЫ
            this.qnt      // САМО КОЛИЧЕСТВО (3)
        ));
        
        super.mc.player.sendMessage(new TextComponentString(TextFormatting.BLUE + "[" + TextFormatting.GOLD + "iCraft" + TextFormatting.BLUE + "] " + TextFormatting.GREEN + ICraftUtils.localize("msg.iCraft.iBay")));
        super.mc.player.closeScreen();
    }
            }
        }
    }
}