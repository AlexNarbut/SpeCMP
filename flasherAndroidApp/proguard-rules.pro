# Стектрейсы release: номера строк остаются, имя исходника скрыто; расшифровка — build/outputs/mapping/<вариант>/mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
# SDK alarmtrade (Kodein-DI) читает generic-сигнатуры TypeReference рефлексией.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
# Аннотации компиляции Tink (security-crypto) в runtime не нужны.
-dontwarn com.google.errorprone.annotations.**
