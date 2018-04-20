package ch.icosys.popjava.core.buffer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.icosys.popjava.core.PopJava;
import ch.icosys.popjava.core.base.MessageHeader;
import ch.icosys.popjava.core.base.POPException;
import ch.icosys.popjava.core.base.POPObject;
import ch.icosys.popjava.core.base.POPSystemErrorCode;
import ch.icosys.popjava.core.combox.Combox;
import ch.icosys.popjava.core.dataswaper.IPOPBase;
import ch.icosys.popjava.core.dataswaper.IPOPBaseInput;
import ch.icosys.popjava.core.util.LogWriter;

/**
 * This abstract class defined all the required methods to implement a buffer.
 * The buffer is responsible to encode and decode the data before sending them
 * or receiving them over the network.
 *
 * Buffer usage: Put the data to be serialized into the buffer and take it out
 * again in the same order.
 * 
 * A special note for Arrays. To put in an array, the putXXArray functions can
 * be used. But to take them out again, first the array length needs to be
 * retrieved with getInt(); <br>
 * Example:
 * 
 * <pre>
 * {@code
 * buffer.putByteArray(byteArray);
 * 
 * byte [] deserialized = buffer.getByteArray(buffer.getInt());
 * }
 * </pre>
 * 
 * <br>
 * Note that null arrays can be stored in the buffer, but will be transformed to
 * arrays of length 0 during deserializiation.
 */
public abstract class POPBuffer {
	/**
	 * Each buffer send must contains a message header
	 */
	protected MessageHeader messageHeader;

	/**
	 * Size of the buffer in byte
	 */
	protected int size = 0;

	/**
	 * Default constructor
	 */
	public POPBuffer() {
		this(new MessageHeader());
	}

	/**
	 * Constructor with given values
	 * 
	 * @param messageHeader
	 *            Message header to be associated with this buffer
	 */
	public POPBuffer(MessageHeader messageHeader) {
		this.messageHeader = messageHeader;
	}

	/**
	 * Erase the buffer and set the pointer to the beginning
	 */
	public abstract void reset();

	/**
	 * Insert a byte in the buffer
	 * 
	 * @param value
	 *            byte value to insert
	 */
	public abstract void put(byte value);

	/**
	 * Insert a boolean in the buffer
	 * 
	 * @param value
	 *            boolean value to insert
	 */
	public abstract void putBoolean(boolean value);

	/**
	 * Insert a char into the buffer
	 * 
	 * @param value
	 *            char value to insert
	 */
	public abstract void putChar(char value);

	/**
	 * Insert a int into the buffer
	 * 
	 * @param value
	 *            int value to insert
	 */
	public abstract void putInt(int value);

	/**
	 * Insert a long into the buffer
	 * 
	 * @param value
	 *            long value to insert
	 */
	public abstract void putLong(long value);

	/**
	 * Insert a short into the buffer
	 * 
	 * @param value
	 *            short value to insert
	 */
	public abstract void putShort(short value);

	/**
	 * Insert a float value into the buffer
	 * 
	 * @param value
	 *            float value to insert
	 */
	public abstract void putFloat(float value);

	/**
	 * Insert a double value into the buffer
	 * 
	 * @param value
	 *            double value to insert
	 */
	public abstract void putDouble(double value);

	/**
	 * Insert a byte array into the buffer
	 * 
	 * @param data
	 *            byte array to insert
	 */
	public abstract void put(byte[] data);

	/**
	 * Insert a byte array into a specific place in the buffer
	 * 
	 * @param data
	 *            byte array to insert
	 * @param offset
	 *            offset for insertion
	 * @param length
	 *            length of the array
	 */
	public abstract void put(byte[] data, int offset, int length);

	/**
	 * Insert a byte array into the buffer
	 * 
	 * @param value
	 *            byte array to insert
	 */
	public abstract void putByteArray(byte[] value);

	/**
	 * Insert a char array into the buffer
	 * 
	 * @param value
	 *            char array to insert
	 */
	public abstract void putCharArray(char[] value);

	/**
	 * Insert a boolean array into the buffer
	 * 
	 * @param value
	 *            boolean array to insert
	 */
	public abstract void putBooleanArray(boolean[] value);

	/**
	 * Insert a int array into the buffer
	 * 
	 * @param value
	 *            int array to insert
	 */
	public abstract void putIntArray(int[] value);

	/**
	 * Insert a short array into the buffer
	 * 
	 * @param value
	 *            short array to insert
	 */
	public abstract void putShortArray(short[] value);

	/**
	 * Insert a long array into the buffer
	 * 
	 * @param value
	 *            long array to insert
	 */
	public abstract void putLongArray(long[] value);

	/**
	 * Insert a float array into the buffer
	 * 
	 * @param value
	 *            float array to insert
	 */
	public abstract void putFloatArray(float[] value);

	/**
	 * Insert a double array into the buffer
	 * 
	 * @param value
	 *            double array to insert
	 */
	public abstract void putDoubleArray(double[] value);

	/**
	 * Retrieve a byte array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return byte array retrieved in the buffer
	 */
	public abstract byte[] getByteArray(int length);

	/**
	 * Retrieve a char array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return char array retrieved in the buffer
	 */
	public abstract char[] getCharArray(int length);

	/**
	 * Retrieve a boolean array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return boolean array retrieved in the buffer
	 */
	public abstract boolean[] getBooleanArray(int length);

	/**
	 * Retrieve a int array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return int array retrieved in the buffer
	 */
	public abstract int[] getIntArray(int length);

	/**
	 * Retrieve a long array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return long array retrieved in the buffer
	 */
	public abstract long[] getLongArray(int length);

	/**
	 * Retrieve a short array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return short array retrieved in the buffer
	 */
	public abstract short[] getShortArray(int length);

	/**
	 * Retrieve a float array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return float array retrieved in the buffer
	 */
	public abstract float[] getFloatArray(int length);

	/**
	 * Retrieve a double array from the buffer
	 * 
	 * @param length
	 *            length of the array to retrieve
	 * @return double array retrieved in the buffer
	 */
	public abstract double[] getDoubleArray(int length);

	/**
	 * Insert a string into the buffer
	 * 
	 * @param value
	 *            string value to insert
	 */
	public abstract void putString(String value);

	/**
	 * Retrieve a byte from the buffer
	 * 
	 * @return byte retrieved in the buffer
	 */
	public abstract byte get();

	/**
	 * Retrieve a boolean from the buffer
	 * 
	 * @return boolean retrieved in the buffer
	 */
	public abstract boolean getBoolean();

	/**
	 * Retrieve a char from the buffer
	 * 
	 * @return char retrieved in the buffer
	 */
	public abstract char getChar();

	/**
	 * Retrieve a int from the buffer
	 * 
	 * @return int retrieved in the buffer
	 */
	public abstract int getInt();

	/**
	 * Retrieve a long from the buffer
	 * 
	 * @return long retrieved in the buffer
	 */
	public abstract long getLong();

	/**
	 * Retrieve a short from the buffer
	 * 
	 * @return short retrieved in the buffer
	 */
	public abstract short getShort();

	/**
	 * Retrieve a float from the buffer
	 * 
	 * @return float retrieved in the buffer
	 */
	public abstract float getFloat();

	/**
	 * Retrieve a double from the buffer
	 * 
	 * @return double retrieved in the buffer
	 */
	public abstract double getDouble();

	/**
	 * Retrieve a string from the buffer
	 * 
	 * @return string retrieved in the buffer
	 */
	public abstract String getString();

	public abstract byte[] array();

	/**
	 * Get a integer value of the byte array
	 * 
	 * @param value
	 *            The byte array to translate
	 * @return The integer
	 */
	public abstract int getTranslatedInteger(byte[] value);

	/**
	 * Retrieve the message header from the buffer
	 * 
	 * @return message header retrieved in the buffer
	 */
	public abstract MessageHeader extractHeader();

	/**
	 * Reset the buffer before reception of a new message
	 */
	public abstract void resetToReceive();

	/**
	 * Pack the message header into the buffer
	 * 
	 * @return number of byte used for the message header
	 */
	public abstract int packMessageHeader();

	/**
	 * Associate a message header with this buffer
	 * 
	 * @param messageHeader
	 *            Message header to be associated with this buffer
	 */
	public void setHeader(MessageHeader messageHeader) {
		this.reset();
		this.messageHeader = messageHeader;
	}

	/**
	 * Get the message header associated with this buffer
	 * 
	 * @return Message header associated with the buffer
	 */
	public MessageHeader getHeader() {
		return messageHeader;
	}

	/**
	 * Get the current size of the buffer
	 * 
	 * @return current size of the buffer as a int value
	 */
	public int size() {
		return size;
	}

	private static final Map<Integer, Constructor<?>> constructorsCache = new ConcurrentHashMap<>();

	private Constructor<?> getConstructorForClass(Class<?> c) throws SecurityException, NoSuchMethodException {
		int hash = c.getName().hashCode();

		if (constructorsCache.containsKey(hash)) {
			return constructorsCache.get(hash);
		}

		Constructor<?> constructor = c.getConstructor();
		constructorsCache.put(hash, constructor);

		return constructor;
	}

	/**
	 * Retrieve an object from the buffer
	 * 
	 * @param c
	 *            Class of the object to retrieve
	 * @return Object retrieved in the buffer
	 * @throws POPException
	 *             thrown if the deserialization process is not going well
	 */
	public Object getValue(Class<?> c) throws POPException {
		return getValue(null, c);
	}

	/**
	 * Retrieve an object from the buffer
	 * 
	 * @param c
	 *            Class of the object to retrieve
	 * @return Object retrieved in the buffer
	 * @throws POPException
	 *             thrown if the deserialization process is not going well
	 */
	@SuppressWarnings("unchecked")
	public Object getValue(Combox sourceCombox, Class<?> c) throws POPException {
		// LogWriter.Prefix="Broker";

		if (c.equals(byte.class) || c.equals(Byte.class)) {
			return get();
		} else if (c.equals(int.class) || c.equals(Integer.class)) {
			return getInt();
		} else if (c.equals(float.class) || c.equals(Float.class)) {
			return getFloat();
		} else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
			return getBoolean();
		} else if (c.equals(String.class)) {
			return getString();
		} else if (c.equals(char.class) || c.equals(Character.class)) {
			return getChar();
		} else if (c.equals(long.class) || c.equals(Long.class)) {
			return getLong();
		} else if (c.equals(double.class) || c.equals(Double.class)) {
			return getDouble();
		} else if (c.equals(short.class) || c.equals(Short.class)) {
			return getShort();
		} else if (c.isArray()) {
			return getArray(c);
		} else if (POPObject.class.isAssignableFrom(c)) {
			return PopJava.newActiveFromBuffer(sourceCombox, c, this);
		} else if (IPOPBase.class.isAssignableFrom(c)) {

			try {
				IPOPBase popBase = (IPOPBase) getConstructorForClass(c).newInstance();
				popBase.deserialize(this);
				return popBase;
			} catch (NoSuchMethodException e) {
				POPException.throwReflectSerializeException(c.getName(), "Default constructor is missing");
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		} else if (IPOPBaseInput.class.isAssignableFrom(c)) {
			try {
				IPOPBaseInput popBase = (IPOPBaseInput) getConstructorForClass(c).newInstance();
				popBase.deserialize(this);
				return popBase;
			} catch (NoSuchMethodException e) {
				POPException.throwReflectSerializeException(c.getName(), "Default constructor is missing");
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		} else if (c.isEnum()) {
			String name = this.getString();
			Class<? extends Enum> t = (Class<? extends Enum>) c;
			return Enum.valueOf(t, name);
		} else if (Serializable.class.isAssignableFrom(c)) {
			int length = getInt();

			byte[] objectContent = getByteArray(length);

			try (ByteArrayInputStream bis = new ByteArrayInputStream(objectContent)) {
				try (ObjectInput in = new ObjectInputStream(bis)) {
					return in.readObject();
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		}

		return null;
	}

	/**
	 * Insert an object into the buffer
	 * 
	 * @param o
	 *            Object to be inserted
	 * @param c
	 *            Class of the object to be inserted
	 * @throws POPException
	 *             thrown if the serialization process is not going well
	 */
	public void putValue(Object o, Class<?> c) throws POPException {
		if (o == null) {
			if (c.isArray()) {
				putArray(o);
			} else {
				throw POPException.throwNullObjectNotAllowException();
			}
		} else if (c.equals(byte.class) || c.equals(Byte.class)) {
			put((Byte) o);
		} else if (c.equals(int.class) || c.equals(Integer.class)) {
			putInt((Integer) o);
		} else if (c.equals(float.class) || c.equals(Float.class)) {
			putFloat((Float) o);
		} else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
			putBoolean((Boolean) o);
		} else if (c.equals(String.class)) {
			putString((String) o);
		} else if (c.equals(char.class) || c.equals(Character.class)) {
			putChar((Character) o);
		} else if (c.equals(long.class) || c.equals(Long.class)) {
			putLong((Long) o);
		} else if (c.equals(double.class) || c.equals(Double.class)) {
			putDouble((Double) o);
		} else if (c.equals(short.class) || c.equals(Short.class)) {
			putShort((Short) o);
		} else if (c.isArray()) {
			putArray(o);
		} else if (IPOPBaseInput.class.isAssignableFrom(c)) {
			try {
				IPOPBaseInput temp = (IPOPBaseInput) o;
				temp.serialize(this);
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		} else if (IPOPBase.class.isAssignableFrom(c)) {
			try {
				IPOPBase temp = (IPOPBase) o;
				temp.serialize(this);
			} catch (Exception e) {
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		} else if (c.isEnum()) {
			putString(((Enum) o).name());
		} else if (Serializable.class.isAssignableFrom(c)) {
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
				try (ObjectOutput out = new ObjectOutputStream(bos)) {
					out.writeObject(o);
					out.flush();

					byte[] objectContent = bos.toByteArray();
					putByteArray(objectContent);
				}
			} catch (IOException e) {
				LogWriter.writeExceptionLog(e);
				POPException.throwReflectSerializeException(c.getName(), e.getMessage());
			}
		} else {
			POPException.throwReflectSerializeException(c.getName(), "Can not serialize parameter " + c.getName());
		}
	}

	/**
	 * Insert an array into the buffer
	 * 
	 * @param o
	 *            Array to be inserted
	 * @throws POPException
	 *             thrown if the serialization process is not going well
	 */
	public void putArray(Object o) throws POPException {
		int length = 0;
		if (o == null) {
			this.putInt(-1);
			return;
		}

		Class<?> c = o.getClass();
		if (c.equals(byte[].class)) {
			putByteArray((byte[]) o);
		} else if (c.equals(int[].class)) {
			putIntArray((int[]) o);
		} else if (c.equals(float[].class)) {
			putFloatArray((float[]) o);
		} else if (c.equals(boolean[].class)) {
			putBooleanArray((boolean[]) o);
		} else if (c.equals(long[].class)) {
			putLongArray((long[]) o);
		} else if (c.equals(double[].class)) {
			putDoubleArray((double[]) o);
		} else if (c.equals(short[].class)) {
			putShortArray((short[]) o);
		} else if (c.equals(char[].class)) {
			putCharArray((char[]) o);
		} else {
			length = Array.getLength(o);
			putInt(length);
			for (int i = 0; i < length; i++) {
				Object element = Array.get(o, i);
				putValue(element, c.getComponentType());
			}
		}

	}

	/**
	 * Retrieve an array from the buffer
	 * 
	 * @param <T>
	 *            the type of return
	 * @param arrayType
	 *            Class of the array to retrieve
	 * @return Array retrieved in the buffer
	 * @throws POPException
	 *             thrown if the serialization process is not going well
	 */
	@SuppressWarnings("unchecked")
	public <T> T getArray(Class<T> arrayType) throws POPException {
		int length = this.getInt();

		if (length < 0) {
			return null;
		} else {
			if (arrayType.equals(byte[].class)) {
				return (T) getByteArray(length);
			} else if (arrayType.equals(int[].class)) {
				return (T) getIntArray(length);
			} else if (arrayType.equals(float[].class)) {
				return (T) getFloatArray(length);
			} else if (arrayType.equals(boolean[].class)) {
				return (T) getBooleanArray(length);
			} else if (arrayType.equals(long[].class)) {
				return (T) getLongArray(length);
			} else if (arrayType.equals(double[].class)) {
				return (T) getDoubleArray(length);
			} else if (arrayType.equals(short[].class)) {
				return (T) getShortArray(length);
			} else if (arrayType.equals(char[].class)) {
				return (T) getCharArray(length);
			} else {
				Class<?> elementType = arrayType.getComponentType();
				Object resultArray = Array.newInstance(elementType, length);
				for (int index = 0; index < length; index++) {
					try {
						Object value = getValue(elementType);
						Array.set(resultArray, index, value);
					} catch (POPException e) {
						throw e;
					}
				}
				return (T) resultArray;
			}

		}
	}

	/**
	 * Insert an object reference into the buffer
	 * 
	 * @param type
	 *            Class of the object
	 * @param obj
	 *            Object to be inserted
	 * @throws POPException
	 *             thrown if the serialization process is not going well
	 */
	public void serializeReferenceObject(Class<?> type, Object obj) throws POPException {
		try {
			if (type.isArray()) {
				putArray(obj);
			} else if (IPOPBase.class.isAssignableFrom(type)) {
				putValue(obj, type);
			}
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve an object reference from the buffer
	 * 
	 * @param type
	 *            Class of the object
	 * @param obj
	 *            Object to be retrieved
	 * @throws POPException
	 *             thrown if the deserialization process is not going well
	 */
	public void deserializeReferenceObject(Class<?> type, Object obj) throws POPException {
		try {
			if (IPOPBase.class.isAssignableFrom(type)) {
				IPOPBase base = (IPOPBase) obj;
				base.deserialize(this);
			} else if (type.isArray()) {

				Object array = getArray(type);

				if (array != null) {
					System.arraycopy(array, 0, obj, 0, Array.getLength(array));
				}
			}
		} catch (Exception e) {
			LogWriter.writeExceptionLog(e);
		}

	}

	/**
	 * Check error code and throw the right exception
	 * 
	 * @param systemErrorCode
	 *            Code of the error
	 * @param buffer
	 *            Buffer from which retrieve the additional informations
	 * @throws POPException
	 *             thrown if any problem occurred
	 */
	public static void checkAndThrow(int systemErrorCode, POPBuffer buffer) throws POPException {
		switch (systemErrorCode) {
		case POPSystemErrorCode.EXCEPTION_INT:
			throw new POPException(systemErrorCode, Integer.toString(buffer.getInt()));
		case POPSystemErrorCode.EXCEPTION_UINT:
			break;
		case POPSystemErrorCode.EXCEPTION_LONG:
			throw new POPException(systemErrorCode, Long.toString(buffer.getLong()));
		case POPSystemErrorCode.EXCEPTION_ULONG:
			throw new POPException(systemErrorCode, "EXCEPTION_ULONG");
		case POPSystemErrorCode.EXCEPTION_SHORT:
			throw new POPException(systemErrorCode, "EXCEPTION_SHORT");
		case POPSystemErrorCode.EXCEPTION_USHORT:
			throw new POPException(systemErrorCode, "EXCEPTION_USHORT");
		case POPSystemErrorCode.EXCEPTION_BOOL:
			throw new POPException(systemErrorCode, Boolean.toString(buffer.getBoolean()));
		case POPSystemErrorCode.EXCEPTION_CHAR:
			throw new POPException(systemErrorCode, Character.toString(buffer.getChar()));
		case POPSystemErrorCode.EXCEPTION_UCHAR:
			throw new POPException(systemErrorCode, "EXCEPTION_UCHAR");
		case POPSystemErrorCode.EXCEPTION_STRING:
			throw new POPException(systemErrorCode, buffer.getString());
		case POPSystemErrorCode.EXCEPTION_FLOAT:
			throw new POPException(systemErrorCode, Float.toString(buffer.getFloat()));
		case POPSystemErrorCode.EXCEPTION_DOUBLE:
			throw new POPException(systemErrorCode, Double.toString(buffer.getDouble()));
		case POPSystemErrorCode.EXCEPTION_OBJECT:
			throw new POPException(systemErrorCode, "EXCEPTION_OBJECT");
		case POPSystemErrorCode.EXCEPTION_PAROC_STD:
			POPException exception = new POPException();
			exception.deserialize(buffer);
			throw new POPException(exception.errorCode, exception.errorMessage + " code " + exception.errorCode);
		}
	}

	/**
	 * Return an empty string
	 * 
	 * @return empty string
	 */
	public String toIntString() {
		return "";
	}

	/**
	 * Return an empty string
	 * 
	 * @return empty string
	 */
	public String toCharString() {
		return "";
	}
}