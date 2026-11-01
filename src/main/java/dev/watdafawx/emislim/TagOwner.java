package dev.watdafawx.emislim;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

/** Added to TagEmiIngredient by mixin. */
public interface TagOwner {
    List<EmiStack> emislim$getStacks();

    void emislim$setStacks(List<EmiStack> stacks);
}
