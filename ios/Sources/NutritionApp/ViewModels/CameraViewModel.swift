import SwiftUI
import AVFoundation

class CameraViewModel: NSObject, ObservableObject, AVCapturePhotoCaptureDelegate {
    @Published var capturedImage: UIImage?
    @Published var isShowingResults = false
    @Published var analysisResult: NutritionAnalysis?

    private var captureSession: AVCaptureSession?
    private var photoOutput: AVCapturePhotoOutput?

    func setupCamera() {
        captureSession = AVCaptureSession()
        guard let captureSession = captureSession else { return }

        guard let backCamera = AVCaptureDevice.default(for: .video) else { return }
        do {
            let input = try AVCaptureDeviceInput(device: backCamera)
            if captureSession.canAddInput(input) {
                captureSession.addInput(input)
            }

            photoOutput = AVCapturePhotoOutput()
            if captureSession.canAddOutput(photoOutput!) {
                captureSession.addOutput(photoOutput!)
            }

            captureSession.startRunning()
        } catch {
            print("Error setting up camera: \(error)")
        }
    }

    func capturePhoto() {
        let settings = AVCapturePhotoSettings()
        photoOutput?.capturePhoto(with: settings, delegate: self)
    }

    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        if let imageData = photo.fileDataRepresentation(),
           let image = UIImage(data: imageData) {
            DispatchQueue.main.async {
                self.capturedImage = image
                self.uploadImage(image)
            }
        }
    }

    private func uploadImage(_ image: UIImage) {
        // Implementar subida al backend
        // Usar NutritionService
        NutritionService.shared.analyzeImage(image) { result in
            DispatchQueue.main.async {
                switch result {
                case .success(let analysis):
                    self.analysisResult = analysis
                    self.isShowingResults = true
                case .failure(let error):
                    print("Error: \(error)")
                }
            }
        }
    }
}