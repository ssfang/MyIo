package org.pc;

public class BinderProxy extends Binder implements IInterface {
  protected final IBinder mRemote;

  public BinderProxy(IBinder mRemote) {
    this.mRemote = mRemote;
  }

  public IBinder remote() {
    return mRemote;
  }

  @Override
  public IBinder asBinder() {
    return mRemote;
  }

  @Override
  protected boolean onTransact(int code, Object data, Object reply, int flags) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }
}
