// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration.kryo;

import com.ahli.galaxy.ui.UIAnimation;
import com.ahli.galaxy.ui.UIAttribute;
import com.ahli.galaxy.ui.UICatalogImpl;
import com.ahli.galaxy.ui.UIConstant;
import com.ahli.galaxy.ui.UIController;
import com.ahli.galaxy.ui.UIFrame;
import com.ahli.galaxy.ui.UIState;
import com.ahli.galaxy.ui.UIStateGroup;
import com.ahli.galaxy.ui.UITemplate;
import com.esotericsoftware.kryo.Kryo;

import java.util.ArrayList;

public class KryoService {
	
	public Kryo getKryoForUICatalog() {
		final Kryo kryo = new Kryo();
		kryo.register(UICatalogImpl.class);
		kryo.register(ArrayList.class);
		kryo.register(UITemplate.class);
		kryo.register(String[].class);
		kryo.register(UIFrame.class);
		kryo.register(UIAttribute.class);
		kryo.register(UIConstant.class);
		kryo.register(UIState.class);
		kryo.register(UIController.class);
		kryo.register(UIAnimation.class);
		kryo.register(UIStateGroup.class);
		kryo.register(KryoGameInfo.class);
		kryo.setReferences(true);
		return kryo;
	}
	
	
	public Kryo getKryoForBaseUiMetaFile() {
		final Kryo kryo = new Kryo();
		kryo.register(KryoGameInfo.class);
		kryo.setReferences(true);
		return kryo;
	}
}
