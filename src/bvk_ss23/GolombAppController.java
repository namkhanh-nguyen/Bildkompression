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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

public class GolombAppController {
	
	private static final String initialFileName = "ara_klein.png";
	private static File fileOpenPath = new File(".");

	private RasterImage sourceImage;
	private String sourceFileName;
	
	private RasterImage preprocessedImage;
	private long preprocessedImageFileSize;
	
	private RasterImage golombImage;

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
    private Label preprocessedInfoLabel;
    
    @FXML
    private ImageView golombImageView;

    @FXML
    private ScrollPane golombScrollPane;

    @FXML
    private Label golombInfoLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Slider zoomSlider;

    @FXML
    private Label zoomLabel;

    @FXML
    void openImage() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(fileOpenPath); 
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
    	File selectedFile = fileChooser.showOpenDialog(null);
    	if(selectedFile != null) {
        	zoomSlider.setValue(1);
    		zoomChanged();
    		fileOpenPath = selectedFile.getParentFile();
    		loadAndDisplayImage(selectedFile);
    		messageLabel.getScene().getWindow().sizeToScene();;
    	}
    }

	@FXML
	public void initialize() {
		loadAndDisplayImage(new File(initialFileName));		
	}
	
 	@FXML
    void zoomChanged() {
    	double zoomFactor = zoomSlider.getValue();
		zoomLabel.setText(String.format("%.1f", zoomFactor));
    	zoom(sourceImageView, sourceScrollPane, zoomFactor);
    	zoom(preprocessedImageView, preprocessedScrollPane, zoomFactor);
    }
	
	private void loadAndDisplayImage(File file) {
		sourceFileName = file.getName();
		messageLabel.setText("Opened image " + sourceFileName);
		sourceImage = new RasterImage(file);
		sourceImage.argb = RasterImage.convertToGrayscale(sourceImage.argb);
		sourceImage.setToView(sourceImageView);
		sourceInfoLabel.setText("");
		preprocessedImage = RasterImage.preprocessImage(sourceImage);
		preprocessedImage.setToView(preprocessedImageView);
		golombImage = RasterImage.processGolombImage(preprocessedImage);
		golombImage.setToView(golombImageView);
		compareImages();
	}
	
	private void compareImages() {
		if(sourceImage.argb.length != preprocessedImage.argb.length || preprocessedImageFileSize == 0) {
			preprocessedInfoLabel.setText("");
			return;
		}
		double mse = preprocessedImage.getMSEfromComparisonTo(sourceImage);
		preprocessedInfoLabel.setText(String.format("MSE = %.1f", mse));
	}
	
	@FXML
	public void saveGolombImage() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(fileOpenPath);
    	fileChooser.setInitialFileName(sourceFileName.substring(0, sourceFileName.lastIndexOf('.')) + ".gol");
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Golomb Images (*.gol)", "*.gol"));
    	File selectedFile = fileChooser.showSaveDialog(null);
    	if(selectedFile != null) {
    		try {
    			DataOutputStream ouputStream = new DataOutputStream(new FileOutputStream(selectedFile));
    			long startTime = System.currentTimeMillis();
    			Golomb.encodeImage(sourceImage, ouputStream);
    			long time = System.currentTimeMillis() - startTime;
    			messageLabel.setText("Encoding in " + time + " ms");
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
	}
	
	@FXML
	public void openGolombImage() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setInitialDirectory(fileOpenPath);
    	fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Golomb Images (*.gol)", "*.gol"));
    	File selectedFile = fileChooser.showOpenDialog(null);
    	if(selectedFile != null) {
    		preprocessedImageFileSize = selectedFile.length();
    		try {
    			DataInputStream inputStream = new DataInputStream(new FileInputStream(selectedFile));
    			long startTime = System.currentTimeMillis();
    			preprocessedImage = Golomb.decodeImage(inputStream);
    			long time = System.currentTimeMillis() - startTime;
    			messageLabel.setText("Decoding in " + time + " ms");
    			preprocessedImage.setToView(preprocessedImageView);
    			compareImages();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
	}
	
		
	private void zoom(ImageView imageView, ScrollPane scrollPane, double zoomFactor) {
		if(zoomFactor == 1) {
			scrollPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
			scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
			imageView.setFitWidth(0);
			imageView.setFitHeight(0);
		} else {
			double paneWidth = scrollPane.getWidth();
			double paneHeight = scrollPane.getHeight();
			double imgWidth = imageView.getImage().getWidth();
			double imgHeight = imageView.getImage().getHeight();
			double lastZoomFactor = imageView.getFitWidth() <= 0 ? 1 : imageView.getFitWidth() / imgWidth;
			if(scrollPane.getPrefWidth() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefWidth(paneWidth);
			if(scrollPane.getPrefHeight() == Region.USE_COMPUTED_SIZE)
				scrollPane.setPrefHeight(paneHeight);
			double scrollX = scrollPane.getHvalue();
			double scrollY = scrollPane.getVvalue();
			double scrollXPix = ((imgWidth * lastZoomFactor - paneWidth) * scrollX + paneWidth/2) / lastZoomFactor;
			double scrollYPix = ((imgHeight * lastZoomFactor - paneHeight) * scrollY + paneHeight/2) / lastZoomFactor;
			imageView.setFitWidth(imgWidth * zoomFactor);
			imageView.setFitHeight(imgHeight * zoomFactor);
			if(imgWidth * zoomFactor > paneWidth)
				scrollX = (scrollXPix * zoomFactor - paneWidth/2) / (imgWidth * zoomFactor - paneWidth);
			if(imgHeight * zoomFactor > paneHeight)
				scrollY = (scrollYPix * zoomFactor - paneHeight/2) / (imgHeight * zoomFactor - paneHeight);
			if(scrollX < 0) scrollX = 0;
			if(scrollX > 1) scrollX = 1;
			if(scrollY < 0) scrollY = 0;
			if(scrollY > 1) scrollY = 1;
			scrollPane.setHvalue(scrollX);
			scrollPane.setVvalue(scrollY);
		}
	}
	

	



}
