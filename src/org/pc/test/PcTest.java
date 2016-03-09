package org.pc.test;

import org.pc.IBinder;
import org.pc.Pc;
import org.pc.ipc.IIpcManager;


public class PcTest {
  public static void main() {
    testInProcess();
  }

  public static void testInProcess() {
    // service side:
    Pc.startIpcManager();
    // create a service
    DemoService service = new DemoService();
    Pc.defaultIpcManager().addService("demo", service);

    // client side:
    IIpcManager ipcmgr = Pc.defaultIpcManager();
    IBinder demoBinder = ipcmgr.getService("demo");
    if (null != demoBinder) {
      IDemo idemo = demoBinder.queryInterface(IDemo.class);
      System.out.println(idemo.add(1, 2));
    }
  }
}
