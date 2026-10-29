# EMI Slim

Client-only NeoForge 1.21.1 mod that cuts EMI 1.1.24's duplicated item data.

- **Tag lists shared:** every `TagEmiIngredient` for the same tag uses one stack list
  (`EmiTags.getValues` is wrapped). When EMI/plugins later change a stack in that list
  (remainders, amount, chance), the owning ingredient gets its own list and the shared
  list gets an unchanged copy, so changes still only reach one recipe. Cleared on tag reload.
- **Component patches interned:** identical `DataComponentPatch` on `ItemEmiStack`s share
  one object (weak interner, reset on each EMI reload). Stacks themselves are never shared.

Off switches: `-Demislim.shareTags=false`, `-Demislim.internPatches=false`. Debug: `-Demislim.debug=true`.

## Build
Copy `emi-1.1.24+1.21.1+neoforge.jar` into `libs/` (compile only), then `gradlew build`.
Smoke test: also put it in `run/mods/`, then `gradlew runSelftest` → `run/emislim-selftest.txt`.

## Measured (TNP Limitless 8, singleplayer, live histogram 60 s after "Reloaded EMI")
| | without | with |
|---|---|---|
| Live heap | 14,209 MB | 13,739 MB (−470 MB) |
| ItemEmiStack | 12.49 M / 600 MB | 6.35 M / 305 MB |
| DataComponentPatch | 2.59 M | 0.16 M |
| SpellContainer | 1.69 M | 28 k |
| Object[] | 1,180 MB | 1,039 MB |

`tools/capture-histo.ps1 -Label x` waits for the game + EMI reload and takes the histogram.
