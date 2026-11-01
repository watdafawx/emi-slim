package dev.watdafawx.emislim;

/** Added to EmiStack by mixin: the shared tag list this stack lives in, or null. */
public interface SharedStack {
    SharedTagList emislim$getHome();

    void emislim$setHome(SharedTagList home);
}
