package test2;
import java.io.IOException;
import java.nio.ByteBuffer;

import test2.SerialManager;
import test2.SerialManager.PacketHandler;


/**
 * @author siggi
 * @date Jun 29, 2013
 */
public class Tester2 implements PacketHandler{

	public static void main(String[] args){
		try {
			new Tester2();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	Tester2() throws IOException, InterruptedException{
		SerialManager serial = new SerialManager("/dev/tty.usbmodem411", 115200, this);

		System.out.println("Tester2 started");
		
		/*
		 * Every second, we send a test packet to the STM32
		 */
		while(true){
//			System.out.println("Tester2 sent packet");
			Thread.sleep(1000);
			
//			serial.addFloatToTxBuffer(1.1f);
			serial.addIntToTxBuffer(102);
			serial.addIntToTxBuffer(103);
			serial.sendTxBuffer();
		}
	}
	
	/*
	 * This method gets called when a packet arrives from the STM32
	 */
	@Override
	public void handlePacket(ByteBuffer packet) {
		System.out.println("Received package of length: "+packet.limit());
		while( packet.remaining() >= 4 ){
			System.out.println( "  "+packet.getInt() );
		}
	}
}
