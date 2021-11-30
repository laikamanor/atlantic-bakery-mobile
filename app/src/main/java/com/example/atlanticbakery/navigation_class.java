package com.example.atlanticbakery;

import java.util.Arrays;
import java.util.List;

public class navigation_class {

    public List<String> getTitles(String appName){
        return Arrays.asList("Settings","Sales","Inventory", "Logs","Production","Count");
//        return Arrays.asList("Settings","Sales","Inventory", "Logs","Count");
    }

    public List<String> getItem(String parentItem){
        if(parentItem.equals("Settings")){
            return Arrays.asList("Cut Off", "Offline Pending Transactions","Change Password", "Logout");
        }else if(parentItem.equals("Sales")) {
            return Arrays.asList("Sales");
        }else if(parentItem.equals("Inventory")){
            return Arrays.asList("Receive from SAP", "System Receive Item","Manual Receive Item", "System Transfer Item","Receive Pullout From Ending Bal","Pending Item Transfer Request");
//            return Arrays.asList("Receive from SAP", "System Receive Item","Manual Receive Item", "System Transfer Item","Receive Pullout From Ending Bal");
        }else if(parentItem.equals("Production")){
            return Arrays.asList("Production Order List", "Goods Issue", "Receive Goods Issue", "Finish Goods Receive");
        }else if(parentItem.equals("Logs")){
            return Arrays.asList("Logs");
        }else if(parentItem.equals("Count")){
            return Arrays.asList("Inventory Count","Inventory Count Variance", "Pull out Request","Pull out Request Variance","Final Count & Pull out Confirmation");
        }else {
            return null;
        }
    }
}
