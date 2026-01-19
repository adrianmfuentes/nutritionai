import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Inicio", systemImage: "house")
                }
            CameraView()
                .tabItem {
                    Label("Capturar", systemImage: "camera")
                }
            HistoryView()
                .tabItem {
                    Label("Historial", systemImage: "list.bullet")
                }
        }
    }
}

#Preview {
    ContentView()
}