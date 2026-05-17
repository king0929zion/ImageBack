# ImageBack 1.0.3

## What's New

- Fixed shortcut creation failure by improving the pin request flow and adding a legacy launcher fallback for launchers that still rely on the older shortcut install path.
- Reworked the app picker so icons load off the main thread, scrolling stays smoother, and each launchable activity is resolved more reliably from the local device app directory.
- Re-tuned the parent app and shortcut badge icon resources so the system badge no longer falls back to an obvious black source icon.

## Release Notes

- Existing home-screen shortcuts need to be removed and recreated once so they can pick up the new transparent source badge behavior.
- If signing secrets are configured, this release publishes a signed release APK.
- If signing secrets are not configured yet, this release publishes a debug-signed APK so it can still be downloaded and tested.
