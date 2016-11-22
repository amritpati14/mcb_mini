package mcbmini.examples;

import java.io.IOException;
import java.util.ArrayList;

import mcbmini.MCBMiniBoard;
import mcbmini.MCBMiniSerialManager;
import mcbmini.MCBMiniServer;
import mcbmini.MCBMiniSerialManager.ResponseType;

/**
 * @author siggi
 * @date Mar 22, 2013
 */
public class SensingServer extends MCBMiniServer{

	/**
	 * @param port_name
	 * @param boards
	 * @throws IOException
	 */
	public SensingServer(String port_name, ArrayList<MCBMiniBoard> boards) throws IOException {
		super(port_name, boards);
	}

	@Override
	protected ResponseType[] createResponseTypes() {
		return new ResponseType[]{
				ResponseType.ACTUAL_TICK_TWO,
				ResponseType.ACTUAL_TICK_TWO,
				ResponseType.ACTUAL_POT_TWO,
				ResponseType.ACTUAL_TICK_TWO,
				ResponseType.ACTUAL_TICK_TWO,
				ResponseType.MOTOR_CURRENT_TWO,
		};
	}
}
