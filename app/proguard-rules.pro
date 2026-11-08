# Add project specific ProGuard rules here.

# Gson models (dev.euryperez.later.data.remote). Fields are kept by Gson's bundled
# rules via @SerializedName. These classes have no no-args constructor and are only
# ever instantiated by Gson (via Unsafe), so without a constructor root R8 full mode
# treats them as never instantiated and strips the field reads. Keep only the
# constructors; names can still be obfuscated.
-keepclassmembers,allowobfuscation class dev.euryperez.later.data.remote.RemoteTask,
    dev.euryperez.later.data.remote.RemoteTaskResponse {
    <init>(...);
}
