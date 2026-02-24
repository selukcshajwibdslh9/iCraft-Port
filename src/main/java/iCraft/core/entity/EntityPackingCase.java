package iCraft.core.entity;

import com.mojang.authlib.GameProfile;
import iCraft.core.register.RegisterBlocks;

import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityPackingCase extends Entity {
    public ItemStack[] storedItems;
    private ItemStack itemStack;
    private boolean hasLanded;
    private GameProfile profile;

    public EntityPackingCase(World world, ItemStack[] storedItems, ItemStack itemStack, UUID id, String name) {
        this(world);
        this.storedItems = storedItems;
        this.itemStack = itemStack;
        this.hasLanded = false;
        this.profile = new GameProfile(id, name);
    }

    public EntityPackingCase(World world) {
        super(world);
    }

    protected void entityInit() {
    }

    protected void readEntityFromNBT(NBTTagCompound nbtTags) {
        NBTTagList tagList = nbtTags.getTagList("Items", 10);
        this.storedItems = new ItemStack[1];

        for (int tagCount = 0; tagCount < tagList.tagCount(); ++tagCount) {
            NBTTagCompound tagCompound = tagList.getCompoundTagAt(tagCount);
            int slotID = tagCompound.getByte("Slot") & 255;
            if (slotID < this.storedItems.length) {
                this.storedItems[slotID] = new ItemStack(tagCompound);
            }
        }

        this.hasLanded = nbtTags.getBoolean("hasLanded");
    }

    protected void writeEntityToNBT(NBTTagCompound nbtTags) {
        NBTTagList tagList = new NBTTagList();

        for (int slotCount = 0; slotCount < this.storedItems.length; ++slotCount) {
            if (this.storedItems[slotCount] != null) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte) slotCount);
                this.storedItems[slotCount].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
        nbtTags.setBoolean("hasLanded", this.hasLanded);
    }

    public void onUpdate() {
        if (this.hasLanded) return;

        if (super.onGround && !super.world.isRemote) {
            int x = MathHelper.floor(super.posX);
            int y = MathHelper.floor(super.posY);
            int z = MathHelper.floor(super.posZ);

            boolean placed = false;

            for (int attempt = 0; attempt < 100; attempt++) {
                BlockPos pos = new BlockPos(x, y + attempt, z);
                IBlockState block = super.world.getBlockState(pos);

                if (block.getMaterial().isReplaceable()) {
                    if (this.placeCase(pos)) {
                        this.hasLanded = true;
                        this.setDead();
                        return;
                    }
                    break;
                }
            }

            this.hasLanded = true;
            this.setDead();
        } else {
            super.motionY = -0.25D;
            this.move(MoverType.SELF, 0.0D, this.motionY, 0.0D);
        }
    }

    /*private void dropItems() {
        if (this.storedItems == null) return;
        for (ItemStack stack : this.storedItems) {
            if (stack != null && !stack.isEmpty()) {
                EntityItem e = new EntityItem(super.world, super.posX, super.posY, super.posZ, stack);
                super.world.spawnEntity(e);
            }
        }
    }*/

    private boolean placeCase(BlockPos pos) {
        if (RegisterBlocks.caseBlock == null) {
            return false;
        }

        super.world.setBlockState(pos, RegisterBlocks.caseBlock.getDefaultState(), 3);
        super.world.removeTileEntity(pos);

        if (this.profile == null || this.itemStack == null) {
            return false;
        }

        this.setDead();
        return true;
    }
}
