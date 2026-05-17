# ImageBack 1.0.2

## What's New

- Fixed the shortcut source badge flow so the launcher badge now points to a nearly invisible transparent host icon instead of a visible black block.
- Refined page transitions and in-screen motion to make the create flow and screen switching feel smoother.
- Reworked the target app picker with async loading, better search ranking, retry handling, and clearer app metadata.

## Release Notes

- Existing home-screen shortcuts need to be removed and recreated once so they can pick up the new transparent source badge behavior.
- If signing secrets are configured, this release publishes a signed release APK.
- If signing secrets are not configured yet, this release publishes a debug-signed APK so it can still be downloaded and tested.
