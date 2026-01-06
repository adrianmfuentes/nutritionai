#!/bin/bash
# Scripts de utilidad para desarrollo

echo "===== Nutrition AI - Scripts de Desarrollo ====="
echo ""
echo "Selecciona una opción:"
echo "1) Compilar y ejecutar app"
echo "2) Limpiar proyecto"
echo "3) Generar APK Debug"
echo "4) Generar APK Release"
echo "5) Ver logs de Android"
echo "6) Instalar en dispositivo"
echo "7) Salir"
echo ""
read -p "Opción: " option

case $option in
  1)
    echo "Compilando y ejecutando..."
    ./gradlew installDebug
    adb shell am start -n com.health.nutritionai/.MainActivity
    ;;
  2)
    echo "Limpiando proyecto..."
    ./gradlew clean
    ;;
  3)
    echo "Generando APK Debug..."
    ./gradlew assembleDebug
    echo "APK generado en: app/build/outputs/apk/debug/"
    ;;
  4)
    echo "Generando APK Release..."
    ./gradlew assembleRelease
    echo "APK generado en: app/build/outputs/apk/release/"
    ;;
  5)
    echo "Mostrando logs (Ctrl+C para salir)..."
    adb logcat | grep -E "NutritionAI|AndroidRuntime"
    ;;
  6)
    echo "Instalando en dispositivo..."
    ./gradlew installDebug
    ;;
  7)
    echo "Saliendo..."
    exit 0
    ;;
  *)
    echo "Opción inválida"
    ;;
esac

