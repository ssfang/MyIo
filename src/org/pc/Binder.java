package org.pc;

public abstract class Binder implements IBinder {
  private IInterface mOwner;

  /**
   * Convenience method for associating a specific interface with the Binder. After calling,
   * queryInterface() will be implemented for you to return the given owner IInterface when the
   * corresponding descriptor is requested.
   */
  public void attachInterface(IInterface owner, String descriptor) {
    mOwner = owner;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IInterface> T queryInterface(Class<T> iinterface) {
    if (null != iinterface && iinterface.isInstance(mOwner)) {
      return (T) mOwner;
    }
    return null;
  }

  protected abstract boolean onTransact(int code, Object data, Object reply, int flags)
      throws Exception;


  @Override
  public int transact(int code, Object data, Object reply, int flags) {
    try {
      return onTransact(code, data, reply, flags) ? 0 : 1;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 1;
  }
}
