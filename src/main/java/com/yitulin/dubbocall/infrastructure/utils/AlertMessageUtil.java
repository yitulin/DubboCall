package com.yitulin.dubbocall.infrastructure.utils;

import com.intellij.openapi.ui.Messages;

/**
 * @author : β‘οΈ
 * description :
 * date : Created in 2021/9/26 11:35 δΈε
 * modified : π§π¨π₯
 */
public class AlertMessageUtil {

    public static void alertInfo(String message,String title){
        Messages.showInfoMessage(message, title);
    }

    public static void alertErrorInfo(String message,String title){
        Messages.showErrorDialog(message, title);
    }

}
