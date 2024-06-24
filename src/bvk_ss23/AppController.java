// BVK Ue1 SS2023 Vorgabe
//
// Copyright (C) 2023 by Klaus Jung
// All rights reserved.
// Date: 2023-03-27

package bvk_ss23;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class AppController
{

    private static final String initialFileName = "ara_klein.png";
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
    private Label sourceInfoLabel;

    @FXML
    private ImageView preprocessedImageView;

    @FXML
    private ScrollPane preprocessedScrollPane;

    @FXML
    private ImageView decodedImageView;

    @FXML
    private ScrollPane decodedScrollPane;

    @FXML
    private Label decodedInfoLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomLabel;

    @FXML
    private ComboBox<?> comboBox;

    @FXML
    private Slider slider;

    @FXML
    private Label mseInfoLabel;

    @FXML
    void openImage()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            this.zoomSlider.setValue(1);
            zoomChanged();
            fileOpenPath = selectedFile.getParentFile();
            loadAndDisplayImage(selectedFile);
            this.messageLabel.getScene().getWindow().sizeToScene();
        }
    }

    @FXML
    public void initialize()
    {
        loadAndDisplayImage(new File(initialFileName));
    }

    @FXML
    void zoomChanged()
    {
        double zoomFactor = this.zoomSlider.getValue();
        Double zoomFactorBoxed = Double.valueOf(zoomFactor);
        this.zoomLabel.setText(String.format("%.1f", zoomFactorBoxed));
        zoom(this.sourceImageView, this.sourceScrollPane, zoomFactor);
        if (this.preprocessedImageView.getImage() != null)
            zoom(this.preprocessedImageView, this.preprocessedScrollPane, zoomFactor);
        if (this.decodedImageView.getImage() != null)
            zoom(this.decodedImageView, this.decodedScrollPane, zoomFactor);

    }

    @FXML
    void golombChanged()
    {
        Double M = Double.valueOf(this.slider.getValue());
        this.decodedInfoLabel.setText(String.format("M = %.0f", M));
    }

    @FXML
    void preprocess()
    {
        if (this.comboBox.getValue().equals("Copy"))
        {
            this.preprocessedImage.setMode(0);
            this.preprocessedImage = this.sourceImage;
        }
        else if (this.comboBox.getValue().equals("DPCM Horizontal"))
        {
            this.preprocessedImage.setMode(2);
            this.preprocessedImage = RasterImage.encodeDPCM(this.sourceImage);
        }

        this.preprocessedImage.setToView(this.preprocessedImageView);
    }

    private void loadAndDisplayImage(File file)
    {
        this.sourceFileName = file.getName();
        this.messageLabel.setText("Opened image " + this.sourceFileName);
        this.sourceImage = new RasterImage(file);
        this.sourceImage = RasterImage.convertToGrayscale(this.sourceImage);
        this.sourceImage.setToView(this.sourceImageView);
        this.sourceInfoLabel.setText("");
        if (this.preprocessedImage == null)
            this.preprocessedImage = this.sourceImage;
        compareImages();
    }

    private void compareImages()
    {
        if (this.sourceImage.argb.length != this.preprocessedImage.argb.length || this.preprocessedImageFileSize == 0)
        {
            this.mseInfoLabel.setText("");
            return;
        }
        Double mse = Double.valueOf(this.preprocessedImage.getMSEfromComparisonTo(this.sourceImage));
        this.mseInfoLabel.setText(String.format("MSE = %.1f", mse));
    }

    @FXML
    public void saveGolombImage()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.setInitialFileName(this.sourceFileName.substring(0, this.sourceFileName.lastIndexOf('.')) + ".gol");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Golomb Images (*.gol)", "*.gol"));
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null)
            try
            {
                DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(selectedFile));
                long startTime = System.currentTimeMillis();
                this.sourceImage.M = this.slider.getValue();
                Golomb.encodeImage(this.sourceImage, outputStream);
                outputStream.close();
                long time = System.currentTimeMillis() - startTime;
                this.messageLabel.setText("Encoding in " + time + " ms");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    }

    @FXML
    public void openGolombImage()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(fileOpenPath);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Golomb Images (*.gol)", "*.gol"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null)
        {
            this.preprocessedImageFileSize = selectedFile.length();
            try
            {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(selectedFile));
                long startTime = System.currentTimeMillis();
                this.rasterImage = Golomb.decodeImage(inputStream);
                inputStream.close();
                long time = System.currentTimeMillis() - startTime;
                this.messageLabel.setText("Decoding in " + time + " ms");
                this.rasterImage.setToView(this.decodedImageView);
                compareImages();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void zoom(ImageView imageView, ScrollPane scrollPane, double zoomFactor)
    {
        if (zoomFactor == 1)
        {
            scrollPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
            scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
            imageView.setFitWidth(0);
            imageView.setFitHeight(0);
        }
        else
        {
            double paneWidth = scrollPane.getWidth();
            double paneHeight = scrollPane.getHeight();
            double imgWidth = imageView.getImage().getWidth();
            double imgHeight = imageView.getImage().getHeight();
            double lastZoomFactor = imageView.getFitWidth() <= 0 ? 1 : imageView.getFitWidth() / imgWidth;
            if (scrollPane.getPrefWidth() == Region.USE_COMPUTED_SIZE)
                scrollPane.setPrefWidth(paneWidth);
            if (scrollPane.getPrefHeight() == Region.USE_COMPUTED_SIZE)
                scrollPane.setPrefHeight(paneHeight);
            double scrollX = scrollPane.getHvalue();
            double scrollY = scrollPane.getVvalue();
            double scrollXPix = ((imgWidth * lastZoomFactor - paneWidth) * scrollX + paneWidth / 2) / lastZoomFactor;
            double scrollYPix = ((imgHeight * lastZoomFactor - paneHeight) * scrollY + paneHeight / 2) / lastZoomFactor;
            imageView.setFitWidth(imgWidth * zoomFactor);
            imageView.setFitHeight(imgHeight * zoomFactor);
            if (imgWidth * zoomFactor > paneWidth)
                scrollX = (scrollXPix * zoomFactor - paneWidth / 2) / (imgWidth * zoomFactor - paneWidth);
            if (imgHeight * zoomFactor > paneHeight)
                scrollY = (scrollYPix * zoomFactor - paneHeight / 2) / (imgHeight * zoomFactor - paneHeight);
            if (scrollX < 0)
                scrollX = 0;
            if (scrollX > 1)
                scrollX = 1;
            if (scrollY < 0)
                scrollY = 0;
            if (scrollY > 1)
                scrollY = 1;
            scrollPane.setHvalue(scrollX);
            scrollPane.setVvalue(scrollY);
        }
    }

}
