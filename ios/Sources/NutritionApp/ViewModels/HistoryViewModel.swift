import SwiftUI

class HistoryViewModel: ObservableObject {
    @Published var meals: [Meal] = []

    func loadMeals() {
        NutritionService.shared.getMeals { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let meals):
                    self.meals = meals
                case .failure(let error):
                    print("Error loading meals: \(error)")
                }
            }
        }
    }
}