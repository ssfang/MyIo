package org.pc;

import java.io.IOException;
import java.net.Socket;

public class TcpTranslator implements IBinder {
  Socket s;

  private TcpTranslator(Socket s) {
    super();
    this.s = s;
    // s.setSoTimeout(1);
  }

  @Override
  public <T extends IInterface> T queryInterface(Class<T> iinterface) {
    return null;
  }

  @Override
  public int transact(int code, Object data, Object reply, int flags) {
    // TODO Auto-generated method stub
    try {
      s.getOutputStream().write(null);
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (null != reply) {
      // wait
      int retCode = -1;
      try {
        retCode = s.getInputStream().read();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      switch (retCode) {
        case 0:// void
          break;
        case 1: // non-void
          break;
        default:// error
          break;
      }
    }
    throw new UnsupportedOperationException();
  }

}
