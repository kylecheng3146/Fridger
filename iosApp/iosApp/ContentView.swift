import UIKit
import SwiftUI
import ComposeApp
import GoogleSignIn

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
    @State private var isSigningIn = false
    @State private var signInError: String? = nil
    @State private var signInSuccess: String? = nil

    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground) // Adapts to light/dark via preferredColorScheme
                .ignoresSafeArea()
            ComposeView(isDark: $isDark)
                .ignoresSafeArea(.keyboard)

            VStack { Spacer() }
        }
        .preferredColorScheme(isDark ? .dark : .light) // Sync status bar + safe areas with app theme
        .onAppear {
            IosGoogleSignInLauncherBridge.setLauncher {
                startGoogleSignIn()
            }
        }
    }

    private func startGoogleSignIn() {
        guard let root = UIApplication.shared.connectedScenes
            .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
            .first?.rootViewController else {
            signInError = "找不到視窗"
            return
        }
        isSigningIn = true
        signInError = nil
        signInSuccess = nil

        GIDSignIn.sharedInstance.signIn(withPresenting: root) { result, error in
            if let error {
                isSigningIn = false
                signInError = error.localizedDescription
                return
            }
            guard let idToken = result?.user.idToken?.tokenString else {
                isSigningIn = false
                signInError = "Missing idToken"
                return
            }
            GoogleSignInBridge().handleIdToken(idToken: idToken) { success, message in
                isSigningIn = false
                if !success {
                    signInError = message ?? "登入失敗"
                } else {
                    signInSuccess = "登入成功"
                }
            }
        }
    }
}
