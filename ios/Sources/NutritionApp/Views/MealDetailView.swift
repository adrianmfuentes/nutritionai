import SwiftUI

struct MealDetailView: View {
    let meal: Meal

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(meal.name)
                    .font(.title)
                Text("Fecha: \(meal.date)")
                Text("Calorías totales: \(Int(meal.totalCalories))")

                ForEach(meal.foods) { food in
                    FoodCard(food: food)
                }
            }
            .padding()
        }
        .navigationTitle("Detalle de Comida")
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
    let sampleMeal = Meal(id: "1", name: "Comida de Prueba", date: "2023-10-01", totalCalories: 250, foods: [sampleFood])
    MealDetailView(meal: sampleMeal)
}