package org.pc;

import org.pc.ipc.IIpcManager;
import org.pc.ipc.IpcManager;
import org.pc.ipc.IpcMangerProxy;
import org.pc.ipc.IpcTranslator;

/**
 * procedure call
 * 
 * @author fangss
 * 
 */
public class Pc {
  public static final String SCHEME_IPC = "ipc://";
  public static final int SCHEME_IPC_LENGTH = SCHEME_IPC.length();
  public static final String SCHEME_TCP = "tcp://";

  private static IIpcManager gDefaultIpcManager;

  public static IIpcManager defaultIpcManager() {
    if (null != gDefaultIpcManager)
      return gDefaultIpcManager;
    // try to get a proxy until it's not null
    while (null == gDefaultIpcManager) {
      // TODO not
      gDefaultIpcManager = new IpcMangerProxy(new IpcTranslator());
      break;
    }
    return gDefaultIpcManager;
  }

  public static void startIpcManager() {
    gDefaultIpcManager = new IpcManager();
  }

  /**
   * 
   * @param address
   * @param clz
   * @return
   */
  public static <T extends IInterface> T getRc(String address, Class<T> clz) {
    if (address.startsWith(SCHEME_IPC)) {
      IBinder binder = defaultIpcManager().getService(address.substring(SCHEME_IPC.length()));
      if (null != binder)
        binder.queryInterface(clz);
    }
    return null;
  }
}
