# Add project specific ProGuard rules here.

# Gson deserializes RemoteTaskResponse/RemoteTask from assets/remote_tasks.json via
# reflection. Keep the classes, their field names (they must match the JSON keys) and
# constructors, and the generic signature of List<RemoteTask>.
-keepattributes Signature, *Annotation*
-keep class dev.euryperez.later.data.remote.RemoteTask { <fields>; <init>(...); }
-keep class dev.euryperez.later.data.remote.RemoteTaskResponse { <fields>; <init>(...); }

# Keep line numbers in release crash stack traces (deobfuscate with mapping.txt).
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
