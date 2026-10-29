package dev.mtop.emislim.mixin;

import dev.emi.emi.registry.EmiStackList;
import dev.mtop.emislim.PatchInterner;
import dev.mtop.emislim.TagShare;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** EmiStackList.clear() runs at the start of every EMI reload (and on leaving a world). */
@Mixin(value = EmiStackList.class, remap = false)
public abstract class EmiStackListMixin {
    @Inject(method = "clear", at = @At("HEAD"))
    private static void emislim$reset(CallbackInfo ci) {
        TagShare.clear();
        PatchInterner.reset();
    }
}
