package dev.watdafawx.emislim.mixin;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.watdafawx.emislim.SharedStack;
import dev.watdafawx.emislim.SharedTagList;
import dev.watdafawx.emislim.TagShare;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * Before a stack that lives in a shared tag list is changed, split it off from the
 * shared list (TagShare.beforeChange). The change itself always runs as normal.
 */
@Mixin(value = EmiStack.class, remap = false)
public abstract class EmiStackMixin implements SharedStack {
    @Shadow
    private EmiStack remainder;
    @Shadow
    protected Comparison comparison;
    @Shadow
    protected long amount;
    @Shadow
    protected float chance;

    @Unique
    private SharedTagList emislim$home;

    @Override
    public SharedTagList emislim$getHome() {
        return emislim$home;
    }

    @Override
    public void emislim$setHome(SharedTagList home) {
        emislim$home = home;
    }

    @Inject(method = "setRemainder(Ldev/emi/emi/api/stack/EmiStack;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"))
    private void emislim$setRemainder(EmiStack stack, CallbackInfoReturnable<EmiStack> cir) {
        if (emislim$home != null && stack != remainder && !(stack.isEmpty() && remainder.isEmpty())) {
            TagShare.beforeChange((EmiStack) (Object) this);
        }
    }

    @Inject(method = "setAmount(J)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"))
    private void emislim$setAmount(long value, CallbackInfoReturnable<EmiStack> cir) {
        if (emislim$home != null && value != amount) {
            TagShare.beforeChange((EmiStack) (Object) this);
        }
    }

    @Inject(method = "setChance(F)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"))
    private void emislim$setChance(float value, CallbackInfoReturnable<EmiStack> cir) {
        if (emislim$home != null && value != chance) {
            TagShare.beforeChange((EmiStack) (Object) this);
        }
    }

    @Inject(method = "comparison(Ldev/emi/emi/api/stack/Comparison;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"))
    private void emislim$comparison(Comparison value, CallbackInfoReturnable<EmiStack> cir) {
        if (emislim$home != null && value != comparison) {
            TagShare.beforeChange((EmiStack) (Object) this);
        }
    }

    @Inject(method = "comparison(Ljava/util/function/Function;)Ldev/emi/emi/api/stack/EmiStack;", at = @At("HEAD"))
    private void emislim$comparisonFn(Function<Comparison, Comparison> fn, CallbackInfoReturnable<EmiStack> cir) {
        if (emislim$home != null) {
            TagShare.beforeChange((EmiStack) (Object) this);
        }
    }
}
