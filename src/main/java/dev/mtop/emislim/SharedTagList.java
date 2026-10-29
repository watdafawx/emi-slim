package dev.mtop.emislim;

import dev.emi.emi.api.stack.EmiStack;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * The one stack list per tag that every TagEmiIngredient of that tag points to.
 * Read-only for everyone else; TagShare can swap a slot when a stack in it is
 * about to be changed (see TagShare.beforeChange).
 */
public final class SharedTagList extends AbstractList<EmiStack> implements RandomAccess {
    private final EmiStack[] stacks;

    SharedTagList(List<EmiStack> source) {
        this.stacks = source.toArray(new EmiStack[0]);
        for (EmiStack s : stacks) {
            ((SharedStack) s).emislim$setHome(this);
        }
    }

    @Override
    public EmiStack get(int index) {
        return stacks[index];
    }

    @Override
    public int size() {
        return stacks.length;
    }

    int indexOfIdentity(EmiStack s) {
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == s) {
                return i;
            }
        }
        return -1;
    }

    EmiStack[] snapshot() {
        return stacks.clone();
    }

    void swap(int index, EmiStack replacement) {
        stacks[index] = replacement;
    }
}
