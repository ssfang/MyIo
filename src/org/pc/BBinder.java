package org.pc;

/**
 * Java没有像C++一样有模板或者像Android的aidl一样自动收集代码，应该继承这个类 BinderNative
 * 
 * @author fangss
 * 
 */
public class BBinder extends Binder implements IInterface {

  public BBinder() {
    attachInterface(this, null);
  }

  public IBinder localBinder() {
    return this;
  }

  /**
   * Cast an IBinder object into an xx interface, generating a proxy if needed.
   * 
   * @param <T>
   */
  public static <T extends IInterface> T asInterface(IBinder obj, Class<T> iinterface) {
    if ((obj == null)) {
      return null;
    }
    return obj.queryInterface(iinterface);
  }

  @Override
  public IBinder asBinder() {
    // TODO: Implement this method
    return this;
  }

  @Override
  protected boolean onTransact(int code, Object data, Object reply, int flags) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }
}
