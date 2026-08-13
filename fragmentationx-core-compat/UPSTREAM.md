# FragmentationX Core compatibility module

This module vendors the Java sources and Android resources from
`me.yokeyword:fragmentationx-core:1.0.2`.

Upstream project: https://github.com/YoKeyword/Fragmentation

Local modification: `androidx.fragment.app.FragmentationMagician` uses cached
reflection with `try/finally` restoration for the private `mStateSaved` and
`mStopped` fields in AndroidX Fragment 1.3.6. This replaces the upstream direct
field access that causes `IllegalAccessError` at runtime.

The upstream source is licensed under the Apache License 2.0; see `LICENSE`.
