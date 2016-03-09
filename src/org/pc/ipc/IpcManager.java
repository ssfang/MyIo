package org.pc.ipc;

import java.util.HashMap;
import java.util.Map;

import org.pc.BBinder;
import org.pc.IBinder;

public class IpcManager extends BBinder implements IIpcManager {

  private final Map<String, IBinder> services = new HashMap<>();

  public IpcManager() {
    attachInterface(this, "IpcManager");
  }

  @Override
  public IpcManager asBinder() {
    return this;
  }

  @Override
  protected boolean onTransact(int code, Object data, Object reply, int flags) throws Exception {
    switch (code) {
      case GET_SERVICE:
        getService((String) data);
        return true;
      case ADD_SERVICE:
        //
        return true;
      case LIST_SERVICES:
        return true;
      default:
        return super.onTransact(code, data, reply, flags);
    }
  }

  @Override
  public IBinder getService(String name) {
    return services.get(name);
  }


  @Override
  public void addService(String name, IBinder service) {
    services.put(name, service);
  }

  @Override
  public String[] listServices() {
    // actual implementation returns null sometimes, so it's ok
    // to return null instead of an empty list.
    return services.keySet().toArray(new String[0]);
  }

}
