package org.pc.ipc;

import org.pc.IBinder;
import org.pc.IInterface;


public interface IIpcManager extends IInterface {
  public static final int GET_SERVICE = 1;
  public static final int ADD_SERVICE = 2;
  public static final int LIST_SERVICES = 3;


  /**
   * Returns a reference to a service with the given name.
   * 
   * @param name the name of the service to get
   * @return a reference to the service, or <code>null</code> if the service doesn't exist
   */
  public IBinder getService(String name);

  /**
   * Place a new @a service called @a name into the service manager.
   * 
   * @param name the name of the new service
   * @param service the service object
   */
  public void addService(String name, IBinder service);

  /**
   * Return a list of all currently running services.
   */
  public String[] listServices();
}
