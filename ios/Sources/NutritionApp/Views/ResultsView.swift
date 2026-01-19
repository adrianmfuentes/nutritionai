import SwiftUI

struct NutritionAnalysis: Codable {
    let is_food: Bool
    let error: String?
    let reasoning: String
    let foods: [FoodItem]
    let meal_analysis: MealAnalysis

    struct FoodItem: Codable {
        let name: String
        let detected_ingredients: [String]
        let portion_display: String
        let portion_grams: Double
        let nutrition: Nutrition
        let category: String
        let confidence: Double

        struct Nutrition: Codable {
            let calories: Double
            let protein: Double
            let carbs: Double
            let fat: Double
            let fiber: Double
        }
    }

    struct MealAnalysis: Codable {
        let health_score: Double
        let health_feedback: String
        let dominant_macro: String
    }
}

struct ResultsView: View {
    let analysis: NutritionAnalysis

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                if analysis.is_food {
                    Text("Análisis Nutricional")
                        .font(.title)
                        .padding(.bottom)

                    ForEach(analysis.foods, id: \.name) { food in
                        FoodCard(food: food)
                    }

                    Text("Razonamiento de la IA")
                        .font(.headline)
                        .padding(.top)
                    Text(analysis.reasoning)
                        .font(.body)
                        .foregroundColor(.secondary)

                    MealSummary(analysis: analysis.meal_analysis)
                } else {
                    Text("No se detectó comida")
                        .font(.title)
                    if let error = analysis.error {
                        Text(error)
                            .foregroundColor(.red)
                    }
                }
            }
            .padding()
        }
    }
}

struct FoodCard: View {
    let food: NutritionAnalysis.FoodItem

    var body: some View {
        VStack(alignment: .leading) {
            Text(food.name)
                .font(.headline)
            Text("Porción: \(food.portion_display)")
            Text("Ingredientes detectados: \(food.detected_ingredients.joined(separator: ", "))")
                .font(.subheadline)
                .foregroundColor(.secondary)
            HStack {
                Text("Calorías: \(Int(food.nutrition.calories))")
                Text("Proteína: \(Int(food.nutrition.protein))g")
                Text("Carbos: \(Int(food.nutrition.carbs))g")
                Text("Grasa: \(Int(food.nutrition.fat))g")
            }
            .font(.caption)
        }
        .padding()
        .background(Color.gray.opacity(0.1))
        .cornerRadius(8)
    }
}

struct MealSummary: View {
    let analysis: NutritionAnalysis.MealAnalysis

    var body: some View {
        VStack(alignment: .leading) {
            Text("Resumen de la Comida")
                .font(.headline)
            Text("Puntuación de Salud: \(Int(analysis.health_score))/100")
            Text(analysis.health_feedback)
            Text("Macro Dominante: \(analysis.dominant_macro)")
        }
    }
}

#Preview {
    let sampleFood = NutritionAnalysis.FoodItem(
        name: "Arroz con Pollo",
        detected_ingredients: ["Arroz", "Pollo", "Guisantes"],
        portion_display: "1 taza (200g)",
        portion_grams: 200,
        nutrition: .init(calories: 250, protein: 15, carbs: 40, fat: 5, fiber: 2),
        category: "mixed",
        confidence: 0.95
    )
    let sampleAnalysis = NutritionAnalysis(
        is_food: true,
        error: nil,
        reasoning: "Se detectó una porción equilibrada de carbohidratos y proteínas.",
        foods: [sampleFood],
        meal_analysis: .init(health_score: 85, health_feedback: "Buen balance de proteínas y carbohidratos.", dominant_macro: "carbs")
    )
    ResultsView(analysis: sampleAnalysis)
}