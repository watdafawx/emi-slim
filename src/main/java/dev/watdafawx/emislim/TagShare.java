package dev.watdafawx.emislim;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.runtime.EmiTagKey;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Phase 1: one stack list per tag, shared by every TagEmiIngredient of that tag.
 *
 * EMI and its plugins sometimes change stacks they got from an ingredient
 * (EmiShapedRecipe.setRemainders, catalyst remainders, setAmount on a picked stack).
 * Without this mod every ingredient had its own stacks, so a change only reached
 * that one recipe. To keep that: right before a shared stack S is changed,
 *  1. the ingredient S was reached through (last getEmiStacks() call on this
 *     thread) gets its own list that still holds S, and
 *  2. the shared list gets an unchanged copy of S in S's slot.
 * Then the change runs on S as normal. Whoever holds S (the caller, the owning
 * ingredient) sees the change; every other user of the tag keeps the old stack.
 */
public final class TagShare {
    public static final boolean ENABLED = !"false".equals(System.getProperty("emislim.shareTags"));
    private static final boolean DEBUG = Boolean.getBoolean("emislim.debug");

    private static final Map<EmiTagKey<?>, SharedTagList> CACHE = new ConcurrentHashMap<>();
    /** Last ingredient whose getEmiStacks() ran on this thread. */
    public static final ThreadLocal<Object> LAST = new ThreadLocal<>();
    /** Bumped on every slot swap; ListEmiIngredients rebuild their flat list when it moved. */
    public static volatile int epoch = 0;

    private static final AtomicLong hits = new AtomicLong();
    private static final AtomicLong built = new AtomicLong();
    private static final AtomicLong swaps = new AtomicLong();
    private static final AtomicLong unknownOwner = new AtomicLong();

    private TagShare() {
    }

    public static List<EmiStack> get(EmiTagKey<?> key, Supplier<List<EmiStack>> original) {
        SharedTagList list = CACHE.get(key);
        if (list != null) {
            hits.incrementAndGet();
            return list;
        }
        List<EmiStack> fresh = original.get();
        if (fresh.isEmpty()) {
            return fresh; // tags not loaded yet: don't cache
        }
        list = new SharedTagList(fresh);
        SharedTagList prev = CACHE.putIfAbsent(key, list);
        if (prev != null) {
            hits.incrementAndGet();
            return prev;
        }
        built.incrementAndGet();
        return list;
    }

    public static void clear() {
        CACHE.clear();
    }

    /** Called right before a stack that lives in a shared list is changed. */
    public static synchronized void beforeChange(EmiStack stack) {
        SharedStack ss = (SharedStack) stack;
        SharedTagList home = ss.emislim$getHome();
        if (home == null) {
            return;
        }
        int idx = home.indexOfIdentity(stack);
        ss.emislim$setHome(null);
        if (idx < 0) {
            return;
        }
        EmiStack[] before = home.snapshot();
        boolean owned = giveOwnerPrivateList(LAST.get(), home, before, stack);
        EmiStack replacement = stack.copy(); // copy() only touches the new object
        ((SharedStack) replacement).emislim$setHome(home);
        home.swap(idx, replacement);
        epoch++;
        swaps.incrementAndGet();
        if (!owned) {
            long n = unknownOwner.getAndIncrement();
            if (DEBUG || n < 3) {
                Object o = LAST.get();
                EmiSlim.LOG.info("EMI Slim: shared tag stack {} changed; owner not found (last ingredient {}). Change stays on that stack only.",
                    stack, o == null ? "none" : o.getClass().getSimpleName(), DEBUG ? new Throwable("stack trace") : null);
            }
        }
    }

    /** Returns true if some ingredient now holds {@code stack} in its own list. */
    private static boolean giveOwnerPrivateList(Object owner, SharedTagList home, EmiStack[] before, EmiStack stack) {
        if (owner instanceof TagEmiIngredient tag) {
            return privatize((TagOwner) tag, home, before, stack);
        }
        if (owner instanceof ListEmiIngredient list) {
            boolean any = false;
            for (EmiIngredient ing : ((ListOwner) list).emislim$getIngredients()) {
                if (ing instanceof TagEmiIngredient tag) {
                    any |= privatize((TagOwner) tag, home, before, stack);
                }
            }
            return any;
        }
        return false;
    }

    private static boolean privatize(TagOwner tag, SharedTagList home, EmiStack[] before, EmiStack stack) {
        List<EmiStack> cur = tag.emislim$getStacks();
        if (cur == home) {
            tag.emislim$setStacks(List.of(before));
            return true;
        }
        // Already has its own list (an earlier change): it holds the stack if it came from here.
        for (EmiStack s : cur) {
            if (s == stack) {
                return true;
            }
        }
        return false;
    }

    public static String stats() {
        return "tag lists built " + built.get() + ", reused " + hits.get() + ", stacks split off on change " + swaps.get()
            + " (owner not found " + unknownOwner.get() + ")";
    }
}
