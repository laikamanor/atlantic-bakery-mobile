package com.example.atlanticbakery.Helper;

import androidx.fragment.app.FragmentManager;

import com.example.atlanticbakery.Interface.NavigationManager;
import com.example.atlanticbakery.OfflineList;

public class FragmentNavigationManager_OfflineList implements NavigationManager {
    private static FragmentNavigationManager_OfflineList mInstance;

    private FragmentManager mFragmentManager;
    public static FragmentNavigationManager_OfflineList getmInstance(OfflineList activity){
        if(mInstance == null)
            mInstance = new FragmentNavigationManager_OfflineList();
        mInstance.configure(activity);
        return  mInstance;
    }

    private void configure(OfflineList activity) {
        activity = activity;
        mFragmentManager = activity.getSupportFragmentManager();

    }

    @Override
    public void showFragment(String title) {

    }

//    @Override
//    public void showFragment(String title) {
//        showFragment(FragmentContent.newInstance(title),false);
//    }
//
//    public void showFragment(Fragment fragment, boolean b){
//        FragmentManager fragmentManager = mFragmentManager;
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.container,fragment);
//        fragmentTransaction.addToBackStack(null);
//        if(b || !BuildConfig.DEBUG)
//            fragmentTransaction.commitAllowingStateLoss();
//        else
//            fragmentTransaction.commit();
//        fragmentManager.executePendingTransactions();
//    }
}
