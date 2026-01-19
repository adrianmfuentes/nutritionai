import SwiftUI

struct HomeView: View {
    var body: some View {
        VStack {
            Text("Bienvenido a Nutrition AI")
                .font(.largeTitle)
                .padding()
            Text("Captura fotos de tu comida para obtener análisis nutricional preciso.")
                .multilineTextAlignment(.center)
                .padding()
            Spacer()
        }
        .padding()
    }
}

#Preview {
    HomeView()
}