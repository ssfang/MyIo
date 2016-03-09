package org.pc.ipc;

import org.pc.BinderProxy;
import org.pc.IBinder;

public class IpcMangerProxy extends BinderProxy implements IIpcManager {

  public IpcMangerProxy(IBinder mRemote) {
    super(mRemote);
  }

  @Override
  public IBinder getService(String name) {
    mRemote.transact(GET_SERVICE, name, null, 0);
    return null;
  }

  @Override
  public void addService(String name, IBinder service) {
    mRemote.transact(ADD_SERVICE, name, null, 0);
  }

  @Override
  public String[] listServices() {
    mRemote.transact(LIST_SERVICES, null, null, 0);
    return null;
  }

}
