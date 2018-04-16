package ch.icosys.popjava.core.buffer;

import ch.icosys.popjava.core.base.MessageHeader;

/**
 * This class defined the interface for each new buffer plug-in
 */
public class BufferPlugin extends POPBuffer {

    
    
	@Override
	public byte[] array() {
		return null;
	}

	@Override
	public MessageHeader extractHeader() {
		return null;
	}

	@Override
	public byte get() {
		return 0;
	}

	@Override
	public boolean getBoolean() {
		return false;
	}

	@Override
	public boolean[] getBooleanArray(int length) {
		return null;
	}

	@Override
	public byte[] getByteArray(int length) {
		return null;
	}

	@Override
	public char getChar() {
		return 0;
	}

	@Override
	public double getDouble() {
		return 0;
	}

	@Override
	public double[] getDoubleArray(int length) {

		return null;
	}

	@Override
	public float getFloat() {

		return 0;
	}

	@Override
	public float[] getFloatArray(int length) {

		return null;
	}

	@Override
	public int getInt() {

		return 0;
	}

	@Override
	public int[] getIntArray(int length) {
		return null;
	}

	@Override
	public long getLong() {
		return 0;
	}

	@Override
	public long[] getLongArray(int length) {
		return null;
	}

	@Override
	public String getString() {
		return null;
	}


	@Override
	public void put(byte value) {

	}

	@Override
	public void put(byte[] data) {

	}

	@Override
	public void put(byte[] data, int offset, int length) {


	}

	@Override
	public void putBoolean(boolean value) {


	}

	@Override
	public void putBooleanArray(boolean[] value) {


	}

	@Override
	public void putByteArray(byte[] value) {

	}

	@Override
	public void putChar(char value) {

	}

	@Override
	public void putDouble(double value) {

	}

	@Override
	public void putDoubleArray(double[] value) {

	}

	@Override
	public void putFloat(float value) {

	}

	@Override
	public void putFloatArray(float[] value) {

	}

	@Override
	public void putInt(int value) {

	}

	@Override
	public void putIntArray(int[] value) {

	}

	@Override
	public void putLong(long value) {

	}

	@Override
	public void putLongArray(long[] value) {

	}

	@Override
	public void putString(String value) {

	}

	@Override
	public void reset() {

	}

	@Override
	public void resetToReceive() {

	}

	@Override
	public int getTranslatedInteger(byte[] value) {
		return 0;
	}

	@Override
	public int packMessageHeader() {
		return 0;
	}

	@Override
	public short getShort() {
		return 0;
	}

	@Override
	public short[] getShortArray(int length) {
		return null;
	}

	@Override
	public void putShort(short value) {

		
	}

	@Override
	public void putShortArray(short[] value) {

		
	}

	@Override
	public char[] getCharArray(int length) {
		return null;
	}

	@Override
	public void putCharArray(char[] value) {
	
		
	}

}
