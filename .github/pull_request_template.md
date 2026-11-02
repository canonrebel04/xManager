## What changed

<!-- One or two sentences. Link backlog IDs from docs/ENHANCEMENTS.md (e.g. REL-12, A11Y-06). -->

## Why

## Testing

- [ ] `./gradlew :app:assembleDebug` builds
- [ ] `./gradlew :app:testDebugUnitTest` passes (new behaviour has a JVM or Robolectric test)
- [ ] `./gradlew :app:lintDebug` reports no new errors
- [ ] CI is green, including the emulator smoke and accessibility (ATF) jobs on API 30 and 34
- [ ] Tried on a device or emulator (say which, or why not needed)

## Checklist

- [ ] `CHANGELOG.md` updated under `[Unreleased]`
- [ ] Backlog items marked done in `docs/ENHANCEMENTS.md`
- [ ] User-visible text is in string resources, not hard-coded
- [ ] Nothing here downloads, patches or installs modified APKs, or touches ads, DRM, licensing or account checks (out of scope for this fork)
