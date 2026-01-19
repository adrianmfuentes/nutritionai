# Nutrition App iOS

Esta es la versión iOS de la aplicación Nutrition AI, desarrollada en Swift y SwiftUI.

## Requisitos

- Xcode 15 o superior
- iOS 15 o superior
- Swift 5.9

## Instalación

1. Clona el repositorio.
2. Abre una terminal en la carpeta `ios/`.
3. Ejecuta `swift build` para construir el proyecto.
4. Abre el proyecto en Xcode: `xed .`

## Estructura del Proyecto

- `Sources/NutritionApp/`: Código fuente
  - `App.swift`: Punto de entrada de la aplicación
  - `Views/`: Vistas de SwiftUI
  - `ViewModels/`: Lógica de vista (MVVM)
  - `Models/`: Modelos de datos
  - `Services/`: Servicios de networking
- `Tests/`: Pruebas unitarias

## Funcionalidades

- Captura de fotos de comida usando la cámara
- Análisis nutricional vía backend
- Historial de comidas
- UI en español

## Configuración del Backend

Asegúrate de que el backend esté corriendo en `http://localhost:3000` o actualiza la URL en `NutritionService.swift`.

## Dependencias

Usa dependencias estándar de iOS: AVFoundation, URLSession. No requiere CocoaPods.
