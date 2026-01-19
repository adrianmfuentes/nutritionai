import Foundation

struct Meal: Identifiable, Codable {
    let id: String
    let name: String
    let date: String
    let totalCalories: Double
    let foods: [NutritionAnalysis.FoodItem]
}