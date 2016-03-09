package org.pc.test;

import org.pc.BBinder;

public class DemoService extends BBinder implements IDemo {

  @Override
  protected boolean onTransact(int code, Object data, Object reply, int flags) throws Exception {
    switch (code) {
      case PUSH:
        push((int) data);
        return true;
      case ALERT:
        alert();
        return true;
      case ADD:
        // add();
        return true;
      default:
        break;
    }
    return super.onTransact(code, data, reply, flags);
  }

  @Override
  public void push(int data) {
    System.out.println("Called, void push(" + data + ");");
  }

  @Override
  public void alert() {
    System.out.println("Called, void alert();");
  }

  @Override
  public int add(int v1, int v2) {
    int sum = v1 + v2;
    System.out.println("Called, int add(" + v1 + ", " + v2 + "); returns " + sum);
    return sum;
  }

}
