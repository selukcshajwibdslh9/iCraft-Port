package iCraft.core.entity;

import com.mojang.authlib.GameProfile;
import iCraft.core.register.RegisterBlocks;
import iCraft.core.tile.TileEntityPackingCase;
import iCraft.core.utils.ICraftUtils;
import iCraft.core.utils.TradeOffer;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityPackingCase extends Entity {
    public ItemStack[] storedItems;
    public int itemIndex = -1;
    public ItemStack itemStack; 
    private boolean hasLanded;
    private GameProfile profile;

    public EntityPackingCase(World world, ItemStack[] storedItems, ItemStack priceStack, UUID playerId, String playerName, int index) {
        super(world);
        this.storedItems = storedItems;
        this.itemStack = priceStack;
        this.itemIndex = index;
        this.profile = new GameProfile(playerId, playerName);
        this.setSize(1.0F, 1.0F);
    }

    public EntityPackingCase(World world) {
        super(world);
        this.setSize(1.0F, 1.0F);
    }

    @Override
    protected void entityInit() {}

@Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("itemIndex", this.itemIndex);
        compound.setBoolean("hasLanded", this.hasLanded);
        if (this.itemStack != null) {
            NBTTagCompound stackTag = new NBTTagCompound();
            this.itemStack.writeToNBT(stackTag);
            compound.setTag("itemStack", stackTag);
            // Добавь это для надежности:
            compound.setInteger("itemCount", this.itemStack.getCount());
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.itemIndex = compound.getInteger("itemIndex");
        this.hasLanded = compound.getBoolean("hasLanded");
        if (compound.hasKey("itemStack")) {
            this.itemStack = new ItemStack(compound.getCompoundTag("itemStack"));
            // Если количество потерялось, восстанавливаем его
            if (compound.hasKey("itemCount")) {
                this.itemStack.setCount(compound.getInteger("itemCount"));
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.hasLanded) return;

        if (this.onGround && !this.world.isRemote) {
            int x = MathHelper.floor(this.posX);
            int y = MathHelper.floor(this.posY);
            int z = MathHelper.floor(this.posZ);

            for (int attempt = 0; attempt < 5; attempt++) {
                BlockPos pos = new BlockPos(x, y + attempt, z);
                IBlockState block = this.world.getBlockState(pos);

                if (block.getMaterial().isReplaceable()) {
                    if (this.placeCase(pos)) return;
                }
            }
            this.setDead();
        } else {
            this.motionY = -0.25D;
            this.move(MoverType.SELF, 0.0D, this.motionY, 0.0D);
        }
    }

private boolean placeCase(BlockPos pos) {
    if (RegisterBlocks.caseBlock == null) return false;
    this.world.setBlockState(pos, RegisterBlocks.caseBlock.getDefaultState(), 3);
    
    TileEntity te = this.world.getTileEntity(pos);
    if (te instanceof TileEntityPackingCase) {
        TileEntityPackingCase caseTe = (TileEntityPackingCase) te;
        if (this.itemIndex >= 0 && this.itemIndex < ICraftUtils.tradeOffers.size()) {
            TradeOffer offer = ICraftUtils.tradeOffers.get(this.itemIndex);
            caseTe.itemIndex = this.itemIndex;
            
            // 1. Товар: Базовое кол-во * множитель
            ItemStack reward = offer.sellStack.copy();
            reward.setCount(offer.sellStack.getCount() * this.itemStack.getCount()); 
            
            // 2. Цена: Базовая цена * множитель
            ItemStack price = offer.buyStack.copy();
            price.setCount(offer.buyStack.getCount() * this.itemStack.getCount());
            
            caseTe.tradeStack = reward;
            caseTe.buyStack = price; // ТУТ ВАЖНО: берем цену из ОФФЕРА, а не просто копию
            
            this.hasLanded = true;
            this.setDead();
            return true;
        }
    }
    return false;
}
}