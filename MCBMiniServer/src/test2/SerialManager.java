package test2;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import mcbmini.serial.PSerial;
import mcbmini.serial.SerialEventHandler;
import mcbmini.serial.iSerial;

/**
 * @author siggi
 * @date Jun 18, 2013
 */
public class SerialManager implements SerialEventHandler {

	private static final boolean DEBUG = true;

	public static interface PacketHandler{
		public void handlePacket(ByteBuffer packet);
	}

	private static final byte HEADER = (byte)0xAA;
	private static final byte ESCAPE = (byte)0xBB;

	private ByteBuffer rxBuffer;
	private byte[] txBytes;
	public ByteBuffer txBuffer;
	private PSerial serial;
	private ByteBuffer conversionBuffer;

	private boolean rxLastReceivedWasEscape;
	private byte rxChecksum;
	private byte txChecksum;

	private PacketHandler packetHandler;

	public SerialManager(String portname, int baudRate, PacketHandler packetHandler) throws IOException{
		this.rxBuffer = ByteBuffer.allocate(256);
		this.rxLastReceivedWasEscape = false;
		this.rxChecksum = 0;

		this.txBytes = new byte[256];
		this.txBuffer = ByteBuffer.wrap(txBytes);
		this.txChecksum = 0;

		this.packetHandler = packetHandler;

		this.conversionBuffer = ByteBuffer.allocate(256);
		this.conversionBuffer.order(ByteOrder.LITTLE_ENDIAN);

		serial = new PSerial(portname, baudRate);
		serial.addSerialEventHandler(this);
	}

	public void handleIncoming(byte readByte){
		// If we receive the header byte then the packet is ready in buffer
		if( readByte == HEADER ){
			// Compare checksums
			byte checksumRcv = rxBuffer.get( rxBuffer.position() - 1 );
			rxBuffer.position( rxBuffer.position()-1 );
			rxChecksum -= checksumRcv;

			// If the checksum's don't match 
			if( rxChecksum != checksumRcv ){
				System.err.println("Bad checksum ! Calculated: "+(rxChecksum&0xff)+", Received: "+(checksumRcv&0xff));
				rxChecksum = 0;
				rxBuffer.clear();
				return;
			}

			// Create packet buffer and handle it
			rxBuffer.flip();
			ByteBuffer packet = ByteBuffer.allocate(rxBuffer.limit());
			packet.order(ByteOrder.LITTLE_ENDIAN);
			packet.put( rxBuffer );
			rxChecksum = 0;
			rxBuffer.clear();
			packet.flip();


			packetHandler.handlePacket(packet);
		}
		// If we receive the escape byte then we have to transform next byte we receive
		else if( readByte == ESCAPE ){
			rxLastReceivedWasEscape = true;
		}
		else{
			// If the last byte we received was the ESCAPE byte then we have to transform this byte
			if( rxLastReceivedWasEscape ){
				readByte ^= (byte)0x01;
				rxLastReceivedWasEscape = false;
			}
			// Finally we add byte to buffer
			rxBuffer.put(readByte);
			rxChecksum += readByte;
		}			
	}

	@Override
	public void handleSerialDataAvailableEvent(iSerial ser) {
		while(ser.available() > 0){
			byte readByte = ser.readByte();
			if( DEBUG ) System.out.println("Received byte: "+(readByte&0xff));

			handleIncoming(readByte);
		}
	}

	public void addIntToTxBuffer(int data){
		conversionBuffer.putInt(data);
		conversionBuffer.flip();
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		conversionBuffer.clear();
	}

	public void addFloatToTxBuffer(float data){
		conversionBuffer.putFloat(data);
		conversionBuffer.flip();
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		addByteToTxBuffer( conversionBuffer.get() );
		conversionBuffer.clear();
	}

	public void addByteToTxBuffer(byte data){
		txChecksum += data;
		if( data == HEADER || data == ESCAPE ){
			data ^= 0x01;
			txBuffer.put( ESCAPE );
			txBuffer.put( data );
		}
		else{
			txBuffer.put( data );
		}
	}

	public void sendTxBuffer() throws IOException{
		addByteToTxBuffer( txChecksum );
		txBuffer.put(HEADER);

		serial.write(txBytes, 0, txBuffer.position());
		txBuffer.clear();
		txChecksum = 0;
	}
}

