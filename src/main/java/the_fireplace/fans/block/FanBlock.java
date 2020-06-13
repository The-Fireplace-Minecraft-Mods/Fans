package the_fireplace.fans.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import the_fireplace.fans.block.entity.FanBlockEntity;

public class FanBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = FacingBlock.FACING;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public FanBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FanBlockEntity) {
                FanBlockEntity fbe = (FanBlockEntity)blockEntity;
                //player.openHandledScreen(fbe);
                if(!fbe.isEmpty()) {
                    ItemScatterer.spawn(world, pos.offset(state.get(FACING)), fbe);
                    fbe.setStack(0, ItemStack.EMPTY);
                    //world.spawnEntity(new ItemEntity(EntityType.ITEM, world, ));
                } else if(!player.getStackInHand(hand).isEmpty()) {
                    fbe.setStack(0, player.getStackInHand(hand).split(1));
                    if(player.getStackInHand(hand).isEmpty())
                        player.setStackInHand(hand, ItemStack.EMPTY);
                }
            }

            return ActionResult.CONSUME;
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        boolean isPowered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.up());
        boolean poweredState = state.get(POWERED);
        if (isPowered && !poweredState)
            world.setBlockState(pos, state.with(POWERED, true), 4);
        else if (!isPowered && poweredState)
            world.setBlockState(pos, state.with(POWERED, false), 4);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new FanBlockEntity();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
}
