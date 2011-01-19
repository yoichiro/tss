/**
 * T'SoundSystem for Java
 */
package org.twintail.tss;

import java.io.IOException;
import java.io.InputStream;

/**
 * class PsglogPlayer
 * 
 * Play AY-3-8910 device control log files.
 * @author Takashi Toyoshima <toyoshim@gmail.com>
 */
public final class PsglogPlayer implements Player {
	private static final int PKT_REGISTER = 0;
	private static final int PKT_VALUE = 1;
	private static final byte PKT_SYNC = -1;
	private static final int BYTE_MASK = 0xff;
	private InputStream input = null;
	private PsgDeviceChannel psg = null;
	private Exception lastException = null;
	private int sync = 0;

	/**
	 * Class constructor.
	 * @param inputStream data input stream
	 * @param psgDeviceChannel AY-3-8910 device to control
	 */
	public PsglogPlayer(final InputStream inputStream,
			final PsgDeviceChannel psgDeviceChannel) {
		input = inputStream;
		psg = psgDeviceChannel;
	}

	/**
	 * Get last happened exception.
	 * @return last happened exception
	 */
	public Exception getLastException() {
		return lastException;
	}

	/**
	 * @see Player
	 */
	public void updateDevice() {
		if (0 != sync) {
			sync--;
			return;
		}
		try {
			byte[] pkt = new byte[2];
			int readByte;
			boolean finished = false;
			do {
				readByte = input.read(pkt, 0, 2);
				if (2 == readByte) {
					if (pkt[PKT_REGISTER] != PKT_SYNC) {
						psg.writeRegister(pkt[PKT_REGISTER],
								pkt[PKT_VALUE] & BYTE_MASK);
					} else {
						sync = pkt[PKT_VALUE] & BYTE_MASK;
						finished = true;
					}
				} else {
					finished = true;
				}
			} while (!finished);
		} catch (IOException e) {
			lastException = e;
		}
	}
}
