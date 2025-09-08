import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    @Binding var isDark: Bool

    func makeUIViewController(context: Context) -> UIViewController {
        // Bridge theme changes from Kotlin to Swift
        let vc = MainViewControllerKt.MainViewController(onThemeChange: { kIsDark in
            let value = kIsDark.boolValue
            DispatchQueue.main.async {
                self.isDark = value
            }
        })
        vc.view.backgroundColor = .clear // Let SwiftUI background show through to edges
        vc.overrideUserInterfaceStyle = .unspecified // Follow parent appearance
        return vc
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @State private var isDark = false

    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground) // Adapts to light/dark via preferredColorScheme
                .ignoresSafeArea()
            ComposeView(isDark: $isDark)
                .ignoresSafeArea(.keyboard)
        }
        .preferredColorScheme(isDark ? .dark : .light) // Sync status bar + safe areas with app theme
    }
}
