import Foundation
import UIKit

class NutritionService {
    static let shared = NutritionService()
    private let baseURL = "http://localhost:3000" // Cambiar a la URL del backend

    func analyzeImage(_ image: UIImage, completion: @escaping (Result<NutritionAnalysis, Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/nutrition/analyze") else { return }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"

        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()
        let fieldName = "image"
        let fileName = "image.jpg"

        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"\(fieldName)\"; filename=\"\(fileName)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        if let imageData = image.jpegData(compressionQuality: 0.8) {
            body.append(imageData)
        }
        body.append("\r\n".data(using: .utf8)!)
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)

        request.httpBody = body

        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let data = data {
                do {
                    let analysis = try JSONDecoder().decode(NutritionAnalysis.self, from: data)
                    completion(.success(analysis))
                } catch {
                    completion(.failure(error))
                }
            }
        }.resume()
    }

    func getMeals(completion: @escaping (Result<[Meal], Error>) -> Void) {
        guard let url = URL(string: "\(baseURL)/meals") else { return }

        URLSession.shared.dataTask(with: url) { data, response, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            if let data = data {
                do {
                    let meals = try JSONDecoder().decode([Meal].self, from: data)
                    completion(.success(meals))
                } catch {
                    completion(.failure(error))
                }
            }
        }.resume()
    }
}