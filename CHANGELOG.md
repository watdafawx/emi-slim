# Changelog

## 0.1.1

- Author is now watdafawx (was mtop); Java packages moved from dev.mtop to dev.watdafawx. No gameplay changes.

## 0.1.0

- First release.
- Cuts EMI's memory use: tag ingredients share one stack list per tag instead of each building its own copy.
- Identical item component data is stored once and shared.
- Client only. Turn either part off with `-Demislim.shareTags=false` / `-Demislim.internPatches=false`.
