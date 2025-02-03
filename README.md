# Image Processing and Analysis Software

This software is designed for advanced image processing and analysis, providing a wide range of algorithms and operations to enhance image quality and extract valuable insights. Below is an overview of the features and components of the software.

## Features

- **Image Loading and Saving**: The software allows users to load images from files and save processed images, making it easy to work with images in different formats.
- **Progress Tracking**: With the `ProgressListener` interface, users can track the progress of ongoing tasks, such as filtering images or computing statistical data.
- **Advanced Image Analysis**: The system includes algorithms for detecting edges, computing image statistics (mean intensity, variance, entropy, etc.), and enhancing image contrast through methods like histogram equalization and CLAHE.
- **Image Clustering**: Supports image segmentation using clustering techniques, including K-means clustering and Otsu's thresholding for automatic image thresholding.

## Key Components

### 1. **Image Processing Algorithms**
- **Canny Edge Detection**: Implements the Canny edge detection algorithm, with methods for non-maximum suppression and hysteresis thresholding to refine edge detection results.
- **Image Statistics**: Calculates various statistical properties of images, including mean intensity, variance, entropy, and contrast.

### 2. **Blur Filters**
The software includes various blur filters for pre-processing images, such as:
- Gaussian blur
- Median blur

These filters are applied through the `BlurFilter` interface, which requires a kernel size as a parameter for processing images.

### 3. **Image Clustering**
Two clustering algorithms are available for segmenting images:
- **K-means clustering**: Implements the K-means algorithm to segment images based on color or intensity.
- **Otsu's thresholding**: A method for automatic image thresholding, commonly used in image segmentation tasks.

### 4. **Edge Detection**
The system offers several edge detection operators, such as:
- **Sobel Operator**
- **Prewitt Operator**
- **Roberts Cross Operator**

Each operator uses different kernels to detect edges in images, helping to identify boundaries and structures within the image.

### 5. **Color Processing**
- **GrayColorFilter**: Converts images to grayscale, which is useful for simplifying images before further analysis.

### 6. **Histogram Processing**
- **CLAHE (Contrast Limited Adaptive Histogram Equalization)**: Enhances image contrast adaptively.
- **Histogram Equalization**: Enhances contrast across the entire image by redistributing pixel intensity values.

### 7. **Memento Pattern for Image State Management**
The software implements the Memento design pattern to manage image state changes:
- **ImageCaretaker**: Saves and restores image states, allowing users to undo and redo changes.
- **ImageMemento**: Stores the state of an image at a particular point in time.
- **ImageOriginator**: Manages the current state of an image and provides methods to save and restore its state.



## Usage

### 1. **Launching the Application**

To start the application:
1. Open the program.
2. The main window will appear with three primary control areas: the Menu Bar (Top), the Toolbar (Left), and the Tool Settings Panel (Right).

### 2. **Loading an Image**

To load an image:
1. Click the **File** menu at the top of the window.
2. Select **Load images** from the dropdown.
3. A file dialog will appear. Choose the image you want to load. Supported formats: **JPG**, **PNG**, **BMP**.
4. After selecting a file, it will open in the main window as a tab. You can open multiple images at once.

### 3. **Selecting a Tool**

To select an image processing tool:
1. Use the **Toolbar** on the left to choose the tool you want (e.g., filters, edge detection, segmentation, etc.).
2. The **Tool Settings Panel** on the right will show the settings for the selected tool.

### 4. **Adjusting Tool Settings**

To adjust the tool settings:
1. After selecting a tool, modify its parameters in the **Tool Settings Panel**.
    - For example, when selecting the **Canny Filter**, you can adjust the blur method, edge detection parameters, kernel size, and segmentation thresholds.
2. Once you have set the desired parameters, click the **Apply** button to process the image with the selected settings.

### 5. **Saving the Processed Image**

To save the processed image:
1. Click the **File** menu.
2. Select **Save file**.
3. A dialog window will appear asking you to choose a directory and name the file. You can save in **PNG**, **JPG**, or **BMP** formats.
4. After saving, the image will be stored in the chosen directory.

### 6. **Undo and Redo Operations**

To undo or redo actions:
1. Use the **Undo** and **Redo** options in the **Menu Bar** to revert or reapply changes made to the image.

### 7. **Viewing Image Statistics**

To view the image statistics:
1. Click on the **Statistics** button in the **Menu Bar** to open a dialog that displays various statistical data for the image, such as mean intensity, variance, entropy, and more.



## Installation

1. Download the JAR file for the software.
2. Make sure you have **Java 18** installed on your system.
    - You can check your Java version by running the command:
      ```
      java -version
      ```

3. Once Java 18 is installed, open your terminal or command prompt.

4. Navigate to the directory where the JAR file is located.

5. Run the software using the following command:
    ```
    java -jar MultimediaProcessingAlgorithm.jar
    ```

