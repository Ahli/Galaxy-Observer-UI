package application.controller;

import application.integration.SettingsIniInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;

public class SettingsGuiToolController extends SettingsAutoSaveController {
	
	@FXML
	private CheckBox buildUnprotectedToo;
	@FXML
	private RadioButton compressMpqBlizz;
	@FXML
	private RadioButton compressMpqNone;
	@FXML
	private RadioButton compressMpqExperimentalBest;
	@FXML
	private CheckBox compressXml;
	@FXML
	private CheckBox checkLayout;
	@FXML
	private CheckBox repairLayoutOrder;
	@FXML
	private CheckBox checkXml;
	
	/**
	 * Automatically called by FxmlLoader
	 */
	@Override
	public void initialize() {
		super.initialize();
		loadValuesFromSettings();
	}
	
	private void loadValuesFromSettings() {
		final SettingsIniInterface settings = app.getIniSettings();
		checkXml.setSelected(settings.isGuiVerifyXml());
		checkLayout.setSelected(settings.isGuiVerifyLayout());
		repairLayoutOrder.setSelected(settings.isGuiRepairLayoutOrder());
		compressXml.setSelected(settings.isGuiCompressXml());
		initCompressMpq(settings.getGuiCompressMpq());
		buildUnprotectedToo.setSelected(settings.isGuiBuildUnprotectedToo());
	}
	
	/**
	 * Applies the selected mode to the radio buttons.
	 *
	 * @param val
	 * 		selected mpq compression mode
	 */
	private void initCompressMpq(int val) {
		switch (val) {
			case 1:
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(true);
				compressMpqExperimentalBest.setSelected(false);
				return;
			case 2:
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(true);
				return;
			default:
				compressMpqNone.setSelected(true);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(false);
		}
	}
	
	@Override
	public void update() {
		super.update();
		loadValuesFromSettings();
	}
	
	@FXML
	public void onCheckXmlClick(ActionEvent actionEvent) {
		boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		app.getIniSettings().setGuiVerifyXml(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCheckLayoutClick(final ActionEvent actionEvent) {
		boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		app.getIniSettings().setGuiVerifyLayout(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onRepairLayoutOrderClick(final ActionEvent actionEvent) {
		boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		app.getIniSettings().setGuiRepairLayoutOrder(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCompressXmlClick(final ActionEvent actionEvent) {
		boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		app.getIniSettings().setGuiCompressXml(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCompressMpqNoneClick() {
		app.getIniSettings().setGuiCompressMpq(0);
		persistSettingsIni();
		initCompressMpq(0);
	}
	
	@FXML
	public void onCompressMpqBlizzClick() {
		app.getIniSettings().setGuiCompressMpq(1);
		persistSettingsIni();
		initCompressMpq(1);
	}
	
	@FXML
	public void onCompressMpqExperimentalBestClick() {
		app.getIniSettings().setGuiCompressMpq(2);
		persistSettingsIni();
		initCompressMpq(2);
	}
	
	@FXML
	public void onBuildUnprotectedTooClick(final ActionEvent actionEvent) {
		boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		app.getIniSettings().setGuiBuildUnprotectedToo(val);
		persistSettingsIni();
	}
}
