package dev.greenhouseteam.enchiridion.mixin.client;

import dev.greenhouseteam.enchiridion.Enchiridion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void enchiridion$displayFrostParticles(CallbackInfo ci) {
        if (level().isClientSide() && isAlive()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.cameraEntity == this && minecraft.options.getCameraType().isFirstPerson())
                return;
            if (Enchiridion.getHelper().isFrozenByEnchantment(this) && !isInPowderSnow && tickCount % 8 == 0 && getTicksFrozen() > getTicksRequiredToFreeze())
                for (int i = 0; i < 4; ++i) {
                    double x = getX() + (getBoundingBox().getXsize() * random.nextDouble());
                    double y = getY(random.nextDouble());
                    double z = getZ() + (getBoundingBox().getZsize() * random.nextDouble());
                    level().addParticle(ParticleTypes.SNOWFLAKE, x, y, z, random.nextDouble() * 0.025, random.nextDouble() * 0.025, random.nextDouble() * 0.025);
                }
        }
    }
}
