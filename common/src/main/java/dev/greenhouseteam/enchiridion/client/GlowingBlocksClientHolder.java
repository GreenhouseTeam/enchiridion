package dev.greenhouseteam.enchiridion.client;

import com.mojang.blaze3d.vertex.*;
import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.client.Camera;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.EnumSet;
import java.util.List;

public class GlowingBlocksClientHolder {

    public static List<BlockPos> outlinedBlocks;
    public static int color;
    public static int duration;
    public static int red;
    public static int green;
    public static int blue;

    private static EntityRenderDispatcher entityRenderer;
    private static final ModelPart.Cube BLOCK = new ModelPart.Cube(
            0, 0,
            0, 0, 0,
            16, 16, 16,
            0, 0, 0,
            false,
            0, 0, EnumSet.allOf(Direction.class));
    private static final RenderType GLOWING = RenderType.outline(Enchiridion.asResource("textures/dummy.png"));

    public static boolean isGlowing() {
        return outlinedBlocks != null && !outlinedBlocks.isEmpty();
    }


    public static void clearGlowingBlocks() {
        outlinedBlocks = List.of();
    }

    public static void setGlowingBlocks(List<BlockPos> outlinedBlocks, int color, int duration) {

        for (BlockPos pos : outlinedBlocks) {
            Enchiridion.LOGGER.info("Block pos: {}", pos);
        }
        GlowingBlocksClientHolder.outlinedBlocks = outlinedBlocks;
        GlowingBlocksClientHolder.color = color;
        GlowingBlocksClientHolder.red = FastColor.ARGB32.red(color);
        GlowingBlocksClientHolder.green = FastColor.ARGB32.green(color);
        GlowingBlocksClientHolder.blue = FastColor.ARGB32.blue(color);
        GlowingBlocksClientHolder.duration = duration;
    }

    public static void tick(ClientLevel clientLevel) {
        if (duration > 0) {
            duration--;
        }
        if (duration == 0) {
            outlinedBlocks = List.of();
        }
    }

    public static void render(GameRenderer gameRenderer, Camera camera, @Nullable PoseStack poseStack, Matrix4f matrix4f, Matrix4f f, @Nullable MultiBufferSource consumers) {
        if (entityRenderer == null) {
            entityRenderer = gameRenderer.getMinecraft().getEntityRenderDispatcher();
        }
        if (poseStack == null && consumers == null) {
            return;
        }

        if (outlinedBlocks != null && !outlinedBlocks.isEmpty()) {
            OutlineBufferSource bufferSource = gameRenderer.getMinecraft().renderBuffers().outlineBufferSource();
            poseStack.pushPose();
            poseStack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
            bufferSource.setColor(red, green, blue, 255);
            var buffer = bufferSource.getBuffer(GLOWING);
            for (BlockPos pos : outlinedBlocks) {
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                BLOCK.compile(
                        poseStack.last(),
                        buffer,
                        0,
                        OverlayTexture.NO_OVERLAY,
                        0, 0, 0, 0
                );
                poseStack.popPose();
            }
            poseStack.popPose();
        }

    }
}
