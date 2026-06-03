package arithmetic_coding;

import java.io.*;

import golomb_coding.Golomb;
import golomb_coding.RasterImage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class ArithmeticEncodingController
{

    private static final String INITIAL_FILE_NAME = "rhino_part.png";
    private static File fileOpenPath = new File(".");

    private RasterImage sourceImage;
    private String sourceFileName;

    private RasterImage preprocessedImage;

    private long preprocessedImageFileSize;

    private RasterImage rasterImage;

    @FXML
    private ImageView sourceImageView;

    @FXML
    private ScrollPane sourceScrollPane;

    @FXML
    private ImageView preprocessedImageView;

    @FXML
    private ScrollPane preprocessedScrollPane;

    @FXML
    private ImageView decodedImageView;

    @FXML
    private ScrollPane decodedScrollPane;

    @FXML
    private Slider tSlider;

    @FXML
    private Label tValueLabel;

    @FXML
    private Label sourceEntropyLabel;

    @FXML
    private Label sourceRawSizeLabel;

    @FXML
    private Label preprocessedEntropyLabel;

    @FXML
    private Label preprocessedEncodedSizeLabel;

    @FXML
    private Label encodingTimeLabel;

    @FXML
    private Label decodedEntropyLabel;

    @FXML
    private Label decodedCompressedSizeLabel;

    @FXML
    private Label decodingTimeLabel;

    @FXML
    public void initialize()
    {
        this.tSlider.setValue(180);
        tSliderChanged();
        resetPlaceholderValues();
        loadAndDisplaySourceImage(new File(INITIAL_FILE_NAME));
    }

    @FXML
    void openSourceImage()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG Images (*.png)", "*.png"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            fileOpenPath = selectedFile.getParentFile();
            loadAndDisplaySourceImage(selectedFile);
        }
    }

    @FXML
    void tSliderChanged()
    {
        this.tValueLabel.setText(String.format("T = %.0f", this.tSlider.getValue()));
        preprocess();
    }

    @FXML
    void preprocess()
    {
        if (this.sourceImage == null)
            return;

        int threshold = (int) Math.round(this.tSlider.getValue());
        RasterImage bitonalImage = new RasterImage(this.sourceImage.width, this.sourceImage.height);

        for (int i = 0; i < this.sourceImage.argb.length; i++)
        {
            int pixel = this.sourceImage.argb[i];
            int alpha = pixel >>> 24 & 0xFF;
            int red = pixel >>> 16 & 0xFF;
            int green = pixel >>> 8 & 0xFF;
            int blue = pixel & 0xFF;
            int gray = (red + green + blue) / 3;
            int bw = gray < threshold ? 0 : 255;
            bitonalImage.argb[i] = alpha << 24 | bw << 16 | bw << 8 | bw;
        }

        this.preprocessedImage = bitonalImage;
        this.preprocessedImage.setToView(this.preprocessedImageView);
    }

    @FXML
    void saveCompressed()
    {
        this.decodingTimeLabel.setText("Decoding time - ms");
    }

    @FXML
    void openCompressed()
    {
        this.decodingTimeLabel.setText("Decoding time - ms");
    }

    private void loadAndDisplaySourceImage(File file)
    {
        long startTime = System.currentTimeMillis();
        this.sourceImage = new RasterImage(file);
        this.sourceImage.setToView(this.sourceImageView);
        syncImagePanelDimensions(this.sourceImage.width, this.sourceImage.height);

        double rawSizeKb = this.sourceImage.width * this.sourceImage.height / 1000.0;
        this.sourceRawSizeLabel.setText(String.format("%.1f kB", rawSizeKb));

        // Keep preprocessing/decoding placeholders empty until arithmetic coding is implemented.
        preprocess();
        this.decodedImageView.setImage(null);
        this.preprocessedEntropyLabel.setText("Entropy = -");
        this.preprocessedEncodedSizeLabel.setText("- / -");
        this.encodingTimeLabel.setText("Encoding time - ms");
        this.decodedEntropyLabel.setText("Entropy = - / MSE = - / PSNR = - dB");
        this.decodedCompressedSizeLabel.setText("-");

        long time = System.currentTimeMillis() - startTime;
        this.decodingTimeLabel.setText("Decoding time " + time + " ms");
    }

    private void syncImagePanelDimensions(int width, int height)
    {
        this.sourceScrollPane.setPrefViewportWidth(width);
        this.sourceScrollPane.setPrefViewportHeight(height);
        this.preprocessedScrollPane.setPrefViewportWidth(width);
        this.preprocessedScrollPane.setPrefViewportHeight(height);
        this.decodedScrollPane.setPrefViewportWidth(width);
        this.decodedScrollPane.setPrefViewportHeight(height);

        // Keep placeholder image views sized consistently until processing/decoding is implemented.
        this.preprocessedImageView.setFitWidth(width);
        this.preprocessedImageView.setFitHeight(height);
        this.decodedImageView.setFitWidth(width);
        this.decodedImageView.setFitHeight(height);
    }

    private void resetPlaceholderValues()
    {
        this.sourceEntropyLabel.setText("Entropy = -");
        this.sourceRawSizeLabel.setText("-");
        this.preprocessedEntropyLabel.setText("Entropy = -");
        this.preprocessedEncodedSizeLabel.setText("- / -");
        this.encodingTimeLabel.setText("Encoding time - ms");
        this.decodedEntropyLabel.setText("Entropy = - / MSE = - / PSNR = - dB");
        this.decodedCompressedSizeLabel.setText("-");
        this.decodingTimeLabel.setText("Decoding time - ms");
    }

    @FXML
    public void openArithmeticImage()
    {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Ari Images (*.ari)", "*.ari"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            this.preprocessedImageFileSize = selectedFile.length();
            try
            {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(selectedFile));
                long startTime = System.currentTimeMillis();
//                this.rasterImage = Golomb.decodeImage(inputStream);
                inputStream.close();
                long time = System.currentTimeMillis() - startTime;
                this.decodingTimeLabel.setText("Decoding in " + time + " ms");
                this.rasterImage.setToView(this.decodedImageView);
                this.decodedCompressedSizeLabel.setText("Decoded size: " + this.preprocessedImageFileSize/1000 + " kB");
//                compareImages();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void saveArithmeticImage()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.setInitialFileName(this.sourceFileName.substring(0, this.sourceFileName.lastIndexOf('.')) + ".ari");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Ari Images (*.ari)", "*.ari"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null)
            try
            {
                DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(selectedFile));
                long startTime = System.currentTimeMillis();
                RasterImage imageToEncode = new RasterImage(this.sourceImage.width, this.sourceImage.height);
                System.arraycopy(this.sourceImage.argb, 0, imageToEncode.argb, 0, this.sourceImage.argb.length);
                Golomb.encodeImage(imageToEncode, outputStream);
                outputStream.close();
                long time = System.currentTimeMillis() - startTime;
                this.encodingTimeLabel.setText("Encoding time " + time + " ms");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    }
}

