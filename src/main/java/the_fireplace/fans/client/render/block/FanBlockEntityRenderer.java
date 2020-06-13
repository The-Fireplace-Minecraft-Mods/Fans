package the_fireplace.fans.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import the_fireplace.fans.block.FanBlock;
import the_fireplace.fans.block.entity.FanBlockEntity;

@Environment(EnvType.CLIENT)
public class FanBlockEntityRenderer extends BlockEntityRenderer<FanBlockEntity> {
    public FanBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    private static final char[] hexLetters = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    @Override
    public void render(FanBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Direction direction = entity.getCachedState().get(FanBlock.FACING);

        ItemStack itemStack = entity.getStack(0);
        if (!itemStack.isEmpty()) {
            matrices.push();
            switch(direction) {
                case UP:
                    matrices.translate(0.5, 1.0001, 0.5);
                    break;
                case DOWN:
                    matrices.translate(0.5, -0.0001, 0.5);
                    break;
                case NORTH:
                    matrices.translate(0.5, 0.5, -0.0001);
                    break;
                case SOUTH:
                    matrices.translate(0.5, 0.5, 1.0001);
                    break;
                case EAST:
                    matrices.translate(1.0001, 0.5, 0.5);
                    break;
                case WEST:
                    matrices.translate(-0.0001, 0.5, 0.5);
                    break;
            }
            Direction direction2 = Direction.fromHorizontal(direction.getHorizontal() % 4);
            float rotation = -direction2.asRotation();
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(rotation));
            if(direction.getAxis().isVertical())
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90.0F));
            matrices.scale(0.8F, 0.8F, 0.8F);
            //TODO find why this doesn't work
            int ll = entity.hasWorld() ? entity.getWorld().getLightLevel(entity.getPos().offset(direction)) : 15;
            char llc = hexLetters[ll];
            //0xF000F0 is full light
            light = Integer.parseInt(llc+"000F0", 16);
            MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformation.Mode.FIXED, light, overlay, matrices, vertexConsumers);
            matrices.pop();
        }
    }
}
