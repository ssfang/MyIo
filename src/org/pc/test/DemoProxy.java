package org.pc.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.pc.BinderProxy;
import org.pc.IBinder;

public class DemoProxy extends BinderProxy implements IDemo {

  public DemoProxy(IBinder mRemote) {
    super(mRemote);
  }

  @Override
  public void push(int data) {
    remote().transact(PUSH, data, null, 0);
  }

  @Override
  public void alert() {
    remote().transact(ALERT, null, null, 0);
  }

  @Override
  public int add(int v1, int v2) {
    AtomicInteger mutable = new AtomicInteger();
    remote().transact(ADD, v1, mutable, 0);
    return mutable.get();
  }
}
