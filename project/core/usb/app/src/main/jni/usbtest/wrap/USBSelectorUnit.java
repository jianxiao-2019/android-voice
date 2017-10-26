/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.extreamsd.usbtester;

public class USBSelectorUnit {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected USBSelectorUnit(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(USBSelectorUnit obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        USBTestNativeJNI.delete_USBSelectorUnit(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void selectInput(int i_inputNr) {
    USBTestNativeJNI.USBSelectorUnit_selectInput(swigCPtr, this, i_inputNr);
  }

  public int getNumberOfInputs() {
    return USBTestNativeJNI.USBSelectorUnit_getNumberOfInputs(swigCPtr, this);
  }

  public int getInputNr(int i_inputNr) {
    return USBTestNativeJNI.USBSelectorUnit_getInputNr(swigCPtr, this, i_inputNr);
  }

}
