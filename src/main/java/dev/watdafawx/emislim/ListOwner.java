package dev.watdafawx.emislim;

import dev.emi.emi.api.stack.EmiIngredient;

import java.util.List;

/** Added to ListEmiIngredient by mixin. */
public interface ListOwner {
    List<? extends EmiIngredient> emislim$getIngredients();
}
