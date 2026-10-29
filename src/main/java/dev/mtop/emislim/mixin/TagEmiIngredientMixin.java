package dev.mtop.emislim.mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.mtop.emislim.TagOwner;
import dev.mtop.emislim.TagShare;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = TagEmiIngredient.class, remap = false)
public abstract class TagEmiIngredientMixin implements TagOwner {
    @Shadow
    private List<EmiStack> stacks;

    /** Remember which ingredient handed out its stacks, so a later change can find its owner. */
    @Inject(method = "getEmiStacks", at = @At("HEAD"))
    private void emislim$trackOwner(CallbackInfoReturnable<List<EmiStack>> cir) {
        if (TagShare.ENABLED) {
            TagShare.LAST.set(this);
        }
    }

    @Override
    public List<EmiStack> emislim$getStacks() {
        return stacks;
    }

    @Override
    public void emislim$setStacks(List<EmiStack> list) {
        stacks = list;
    }
}
