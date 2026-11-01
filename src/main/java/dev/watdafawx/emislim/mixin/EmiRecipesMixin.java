package dev.watdafawx.emislim.mixin;

import dev.emi.emi.registry.EmiRecipes;
import dev.watdafawx.emislim.EmiSlim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiRecipes.class, remap = false)
public abstract class EmiRecipesMixin {
    @Inject(method = "bake", at = @At("TAIL"))
    private static void emislim$logStats(CallbackInfo ci) {
        EmiSlim.logStats();
    }
}
