package iCraft.core.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPackingCase extends BlockHorizontal {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockPackingCase() {
        super(Material.WOOD);
        this.setCreativeTab(CreativeTabs.MISC);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        // Убрали TYPE, оставили только FACING. Больше никаких крашей по метадате!
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true; // Включаем поддержку хранилища данных
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new iCraft.core.tile.TileEntityPackingCase(); // Твой класс с данными
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override
    public boolean isFullCube(IBlockState state) { return false; }
    
@Override
public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    if (worldIn.isRemote) return true;

    TileEntity te = worldIn.getTileEntity(pos);
    if (!(te instanceof iCraft.core.tile.TileEntityPackingCase)) return true;
    
    iCraft.core.tile.TileEntityPackingCase caseTe = (iCraft.core.tile.TileEntityPackingCase) te;
    ItemStack heldItem = playerIn.getHeldItem(hand);

    // 1. Если рука пуста — открываем GUI
    if (heldItem.isEmpty()) {
        playerIn.openGui(iCraft.core.ICraft.instance, 9, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    // 2. Если в ящике пусто — выходим
    if (caseTe.tradeStack.isEmpty() || caseTe.buyStack.isEmpty()) {
        playerIn.sendMessage(new TextComponentString("V jashike pustota"));
        return true;
    }

    // 3. Логика покупки
if (heldItem.isItemEqual(caseTe.buyStack)) {
    // Теперь берем цену за 1 шт и умножаем на количество, выбранное в GUI
    int pricePerUnit = caseTe.buyStack.getCount() * caseTe.amountToBuy; 
    
    if (heldItem.getCount() < pricePerUnit) {
        playerIn.sendMessage(new TextComponentString("Мало денег, надо: " + pricePerUnit));
        return true;
    }

    heldItem.shrink(pricePerUnit);
    
    ItemStack reward = caseTe.tradeStack.copy();
    reward.setCount(caseTe.tradeStack.getCount() * caseTe.amountToBuy); // Выдаем умноженное кол-во
    // Добавь это перед spawnEntity
playerIn.sendMessage(new TextComponentString("DEBUG: amountToBuy v bloke = " + caseTe.amountToBuy));
playerIn.sendMessage(new TextComponentString("DEBUG: nagrada kolishestvo = " + caseTe.tradeStack.getCount()));
    
    worldIn.spawnEntity(new net.minecraft.entity.item.EntityItem(worldIn, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, reward));
    worldIn.setBlockToAir(pos);
    return true;

    } else {
        playerIn.sendMessage(new TextComponentString("item ne podhodid"));
        return true;
    }
}}