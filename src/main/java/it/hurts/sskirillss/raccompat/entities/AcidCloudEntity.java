package it.hurts.sskirillss.raccompat.entities;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import it.hurts.sskirillss.raccompat.items.AcidWalkerItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

public class AcidCloudEntity extends ThrowableProjectile {
    public AcidCloudEntity(EntityType<? extends AcidCloudEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        for (int i = 0; i < 2; i++)
            level().addParticle(ACParticleRegistry.GAMMAROACH.get(), this.getX() + (MathUtils.randomFloat(random) * (this.getBbWidth() / 2F)), this.getY() + 0.1F,
                    this.getZ() + (MathUtils.randomFloat(random) * (this.getBbWidth() / 2F)), MathUtils.randomFloat(random) * 0.05F, 0, MathUtils.randomFloat(random) * 0.05F);

        if (this.tickCount % 5 == 0 && random.nextInt(4) == 0)
            level().addParticle(ACParticleRegistry.ACID_BUBBLE.get(), this.getX() + (MathUtils.randomFloat(random) * (this.getBbWidth() / 2F)), this.getY() + 0.1F,
                    this.getZ() + (MathUtils.randomFloat(random) * (this.getBbWidth() / 2F)), MathUtils.randomFloat(random) * 0.05F, random.nextFloat() * 0.1, MathUtils.randomFloat(random) * 0.05F);

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()))
            AcidWalkerItem.triggerAcidEffect(entity);

        if (this.tickCount >= 100)
            this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.setDeltaMovement(0, 0, 0);

        this.setNoGravity(true);
    }

    @Override
    protected float getGravity() {
        return 0.01F;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Nonnull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {

    }
}
