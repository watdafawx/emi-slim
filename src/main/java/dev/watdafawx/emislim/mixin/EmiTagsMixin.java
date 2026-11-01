package dev.watdafawx.emislim.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiTagKey;
import dev.watdafawx.emislim.TagShare;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EmiTags.class, remap = false)
public abstract class EmiTagsMixin {
    /** One shared stack list per tag instead of a fresh list for every TagEmiIngredient. */
    @WrapMethod(method = "getValues")
    private static List<EmiStack> emislim$shareValues(EmiTagKey<?> key, Operation<List<EmiStack>> original) {
        if (!TagShare.ENABLED) {
            return original.call(key);
        }
        return TagShare.get(key, () -> original.call(key));
    }

    /** Tag contents change on reload: never share lists across it. */
    @Inject(method = "reload", at = @At("HEAD"))
    private static void emislim$clearOnReload(CallbackInfo ci) {
        TagShare.clear();
    }
}
