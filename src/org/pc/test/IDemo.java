package org.pc.test;

import org.pc.IInterface;

/**
 * <pre>
 * +-----------+         +------------+ 
 * |  IBinder  |         | IInterface | 
 * +-----------+         +------------+  
 *      |implements        |implements 
 * +----------+          +------------+
 * |  Binder  |        +-|    IXX     |
 * +----------+        | +------------+
 *      |extends       |       |implements 
 * +----------+        | +------------+
 * | XXNative |--------+ |  XXProxy   |
 * +----------+          +------------+
 *      |extends
 * +----------+
 * | XXService|
 * +----------+
 * </pre>
 * 
 * @author fangss
 * 
 */
public interface IDemo extends IInterface {
  public static final int ALERT = 1;
  public static final int PUSH = 2;
  public static final int ADD = 3;

  // Sends a user-provided value to the service
  void push(int data);

  // Sends a fixed alert string to the service
  void alert();

  // Requests the service to perform an addition and return the result
  int add(int v1, int v2);
}
