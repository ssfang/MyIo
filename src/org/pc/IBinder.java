package org.pc;



public interface IBinder {
  /**
   * 
   * @param <T>
   * @param iinterface
   * @return
   */
  public <T extends IInterface> T queryInterface(Class<T> iinterface);

  public int transact(int code, Object data, Object reply, int flags);
}
