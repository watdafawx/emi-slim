package dev.mtop.emislim.mixin;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.mtop.emislim.ListOwner;
import dev.mtop.emislim.TagShare;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * fullList is a flat copy of the child ingredients' stacks. When a shared tag list
 * swaps a slot (TagShare.beforeChange), the flat copy is rebuilt on next use so it
 * matches the child lists again.
 */
@Mixin(value = ListEmiIngredient.class, remap = false)
public abstract class ListEmiIngredientMixin implements ListOwner {
    @Shadow
    @Final
    private List<? extends EmiIngredient> ingredients;

    @Shadow
    @Final
    @Mutable
    private List<EmiStack> fullList;

    @Unique
    private boolean emislim$hasShared;
    @Unique
    private int emislim$epoch;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void emislim$init(List<? extends EmiIngredient> list, long amount, CallbackInfo ci) {
        emislim$epoch = TagShare.epoch;
        for (EmiIngredient ing : ingredients) {
            if (ing instanceof TagEmiIngredient) { // may hold shared stacks (even after its own split)
                emislim$hasShared = true;
                break;
            }
        }
    }

    @Inject(method = "getEmiStacks", at = @At("HEAD"))
    private void emislim$beforeGet(CallbackInfoReturnable<List<EmiStack>> cir) {
        if (emislim$hasShared && emislim$epoch != TagShare.epoch) {
            emislim$epoch = TagShare.epoch;
            fullList = ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
        }
        if (TagShare.ENABLED) {
            TagShare.LAST.set(this);
        }
    }

    @Override
    public List<? extends EmiIngredient> emislim$getIngredients() {
        return ingredients;
    }
}
