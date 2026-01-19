// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NutritionApp",
    platforms: [.iOS(.v15)],
    products: [
        .executable(name: "NutritionApp", targets: ["NutritionApp"]),
    ],
    targets: [
        .executableTarget(
            name: "NutritionApp",
            dependencies: []
        ),
        .testTarget(
            name: "NutritionAppTests",
            dependencies: ["NutritionApp"]
        ),
    ]
)