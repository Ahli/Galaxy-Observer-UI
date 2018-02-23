package application.ui.settings;

import application.integration.SettingsIniInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;

public class SettingsCommandLineToolController extends SettingsAutoSaveController {
	
	@FXML
	private CheckBox heroesPtrActive;
	@FXML
	private CheckBox buildUnprotectedToo;
	@FXML
	private RadioButton compressMpqBlizz;
	@FXML
	private RadioButton compressMpqNone;
	@FXML
	private RadioButton compressMpqExperimentalBest;
	@FXML
	private RadioButton compressMpqSystemDefault;
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
		final SettingsIniInterface settings = configService.getIniSettings();
		checkXml.setSelected(settings.isCmdLineVerifyXml());
		checkLayout.setSelected(settings.isCmdLineVerifyLayout());
		repairLayoutOrder.setSelected(settings.isCmdLineRepairLayoutOrder());
		compressXml.setSelected(settings.isCmdLineCompressXml());
		initCompressMpq(settings.getCmdLineCompressMpq());
		buildUnprotectedToo.setSelected(settings.isCmdLineBuildUnprotectedToo());
		heroesPtrActive.setSelected(settings.isHeroesPtrActive());
	}
	
	/**
	 * Applies the selected mode to the radio buttons.
	 *
	 * @param val
	 * 		selected mpq compression mode
	 */
	private void initCompressMpq(final int val) {
		switch (val) {
			case 1:
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(true);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(false);
				return;
			case 2:
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(true);
				compressMpqSystemDefault.setSelected(false);
				return;
			case 3:
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(true);
				return;
			default:
				compressMpqNone.setSelected(true);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(false);
		}
	}
	
	@Override
	public void update() {
		super.update();
		loadValuesFromSettings();
	}
	
	@FXML
	public void onCheckXmlClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setCmdLineVerifyXml(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCheckLayoutClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setCmdLineVerifyLayout(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onRepairLayoutOrderClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setCmdLineRepairLayoutOrder(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCompressXmlClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setCmdLineCompressXml(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onCompressMpqNoneClick() {
		clickedCompressionMode(0);
	}
	
	/**
	 * @param i
	 */
	private void clickedCompressionMode(final int i) {
		configService.getIniSettings().setGuiCompressMpq(i);
		persistSettingsIni();
		initCompressMpq(i);
	}
	
	@FXML
	public void onCompressMpqBlizzClick() {
		clickedCompressionMode(1);
	}
	
	@FXML
	public void onCompressMpqExperimentalBestClick() {
		clickedCompressionMode(2);
	}
	
	@FXML
	public void onCompressMpqSystemDefaultClick() {
		clickedCompressionMode(3);
	}
	
	@FXML
	public void onBuildUnprotectedTooClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setCmdLineBuildUnprotectedToo(val);
		persistSettingsIni();
	}
	
	@FXML
	public void onHeroesPtrActiveClick(final ActionEvent actionEvent) {
		final boolean val = ((CheckBox) actionEvent.getSource()).selectedProperty().getValue();
		configService.getIniSettings().setIsHeroesPtrActive(val);
		persistSettingsIni();
	}
}
