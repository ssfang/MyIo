package org.pc;

/**
 * Base class for Binder interfaces. When defining a new interface, you must derive it from
 * IInterface.
 * 
 * @see android.os.IInterface
 */
public interface IInterface {
  /**
   * Retrieve the Binder object associated with this interface. You must use this instead of a plain
   * cast, so that proxy objects can return the correct result.
   */
  public IBinder asBinder();
}
