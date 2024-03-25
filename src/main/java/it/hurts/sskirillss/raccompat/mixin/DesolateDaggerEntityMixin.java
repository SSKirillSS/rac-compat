package it.hurts.sskirillss.raccompat.mixin;

import com.github.alexmodguy.alexscaves.server.entity.item.DesolateDaggerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DesolateDaggerEntity.class)
public class DesolateDaggerEntityMixin {
    @ModifyVariable(method = "tick", name = "damage", at = @At("STORE"))
    public float onDamageCalculate(float original) {
        return original + ((DesolateDaggerEntity) (Object) this).getPersistentData().getFloat("raccompat_damage");
    }
}