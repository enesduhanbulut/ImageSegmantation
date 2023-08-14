# Image Segmentation Classifier

This project provides a REST API for image segmentation using a pre-trained neural network model. The API takes in an image and returns segmented images based on the model's predictions.

## Prerequisites

- Java 8 or later
- Spring Boot
- PyTorch (for the model inference)
- Gradle (for building the project)
- Docker (optional, for containerizing the application)

## Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/yourusername/image-segmentation-classifier.git
2. Download Pytorch lib and export
  ```bash
  https://pytorch.org/get-started/locally/)https://pytorch.org/get-started/locally/
```

## Usage
1. Open the project in your preferred IDE.
2. Run the Spring Boot application with in terminal
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.library.path=/export/pytorch/path/libtorch/lib"
  
Use an API client (e.g., cURL, Postman) to send POST requests to the API endpoint for image segmentation.

## API Documentation
Endpoint: POST /api/classifier/predict
### Request Body
```
{
    "modelName": "modelmobile256_256.ptl",
    "image": "base64_encoded_image_data_here"
}
```

1. modelName: The name of the pre-trained model.

2. image: Base64-encoded image data.
### Response
The response will be a JSON object containing segmented images in base64-encoded format.

## Contributing
1. Contributions are welcome! If you find a bug or want to add a new feature, feel free to open an issue or submit a pull request.
2. Fork the repository.
3. Create a new branch for your feature/bugfix: git checkout -b feature-name
4. Commit your changes: git commit -am 'Add new feature'
5. Push to the branch: git push origin feature-name
6. Open a pull request.
7. Please follow the existing code style and provide clear commit messages.



