package org.pc.ipc;

import org.pc.IBinder;
import org.pc.IInterface;

public class IpcTranslator implements IBinder {

  @Override
  public <T extends IInterface> T queryInterface(Class<T> iinterface) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int transact(int code, Object data, Object reply, int flags) {
    throw new UnsupportedOperationException();
  }
}
