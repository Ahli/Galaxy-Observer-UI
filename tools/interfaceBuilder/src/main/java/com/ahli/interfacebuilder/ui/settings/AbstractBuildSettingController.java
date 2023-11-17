package com.ahli.interfacebuilder.ui.settings;

import com.ahli.interfacebuilder.config.ConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;

public class AbstractBuildSettingController extends SettingsAutoSaveController {
	@FXML
	private RadioButton compressMpqBlizz;
	@FXML
	private RadioButton compressMpqNone;
	@FXML
	private RadioButton compressMpqExperimentalBest;
	@FXML
	private RadioButton compressMpqSystemDefault;
	
	public AbstractBuildSettingController(final ConfigService configService) {
		super(configService);
	}
	
	/**
	 * Applies the selected mode to the radio buttons.
	 *
	 * @param val
	 * 		selected mpq compression mode
	 */
	protected void initCompressMpq(final int val) {
		switch (val) {
			case 1 -> {
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(true);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(false);
			}
			case 2 -> {
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(true);
				compressMpqSystemDefault.setSelected(false);
			}
			case 3 -> {
				compressMpqNone.setSelected(false);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(true);
			}
			default -> {
				compressMpqNone.setSelected(true);
				compressMpqBlizz.setSelected(false);
				compressMpqExperimentalBest.setSelected(false);
				compressMpqSystemDefault.setSelected(false);
			}
		}
	}
}
