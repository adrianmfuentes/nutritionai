import SwiftUI

struct HistoryView: View {
    @StateObject private var viewModel = HistoryViewModel()

    var body: some View {
        NavigationView {
            List(viewModel.meals) { meal in
                NavigationLink(destination: MealDetailView(meal: meal)) {
                    VStack(alignment: .leading) {
                        Text(meal.name)
                            .font(.headline)
                        Text("Fecha: \(meal.date)")
                            .font(.subheadline)
                        Text("Calorías totales: \(Int(meal.totalCalories))")
                    }
                }
            }
            .navigationTitle("Historial de Comidas")
            .onAppear {
                viewModel.loadMeals()
            }
        }
    }
}

#Preview {
    HistoryView()
}