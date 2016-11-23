// IWorkService.aidl
package com.broadsense.newpine.launcher;

// Declare any non-default types here with import statements

interface IWorkService {
    /**
     * 传入包名  ，和是否工作的状态
     */
   void isWork(String packageName , boolean isWork);
}
