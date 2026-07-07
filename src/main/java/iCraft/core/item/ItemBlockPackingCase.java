package iCraft.core.item;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockPackingCase extends ItemBlock {
    public ItemBlockPackingCase(Block block) {
        super(block);
        this.setHasSubtypes(true); // Разрешаем разные типы
    }
    @Override
    public int getMetadata(int damage) {
        return damage; // Пробрасываем метадату
    }
}