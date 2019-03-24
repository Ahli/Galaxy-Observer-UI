package interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UICatalogImpl;

public class KryoCachedBaseUi {
	
	private KryoGameInfo gameInfo;
	
	private UICatalogImpl catalog;
	
	public KryoCachedBaseUi() {
	}
	
	public KryoCachedBaseUi(final KryoGameInfo gameInfo, final UICatalogImpl catalog) {
		setGameInfo(gameInfo);
		setCatalog(catalog);
	}
	
	public KryoGameInfo getGameInfo() {
		return gameInfo;
	}
	
	public void setGameInfo(final KryoGameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}
	
	public UICatalogImpl getCatalog() {
		return catalog;
	}
	
	public void setCatalog(final UICatalogImpl catalog) {
		this.catalog = catalog;
	}
}
