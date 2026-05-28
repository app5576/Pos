#!/bin/bash
# سكربت إعادة بناء مشروع MicroPOS

echo "🧹 تنظيف المشروع..."
./gradlew clean

echo ""
echo "📦 إعادة بناء المشروع..."
./gradlew assembleDebug --stacktrace

echo ""
echo "✅ تم الانتهاء!"
